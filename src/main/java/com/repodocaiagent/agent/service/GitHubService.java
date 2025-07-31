// src/main/java/com/repodocaiagent/agent/service/GitHubService.java
package com.repodocaiagent.agent.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Service to handle GitHub repository operations.
 */
public class GitHubService {
    private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);
    private final Dotenv dotenv;

    public GitHubService(Dotenv dotenv) {
        this.dotenv = dotenv;
    }

    /**
     * Clones a GitHub repository to the exact target directory (overwriting it if it exists).
     *
     * @param repoUrl   GitHub repository URL (e.g., "https://github.com/username/repo.git")
     * @param targetDir Final directory into which the repo will live (must contain .git, src/, pom.xml, etc)
     * @return Path to the cloned repository (same as targetDir)
     */
    public Path cloneRepository(String repoUrl, Path targetDir) throws GitAPIException, IOException {
        // ensure a clean slate
        if (Files.exists(targetDir)) {
            logger.info("Target directory exists, cleaning it up: {}", targetDir);
            cleanupDirectory(targetDir);
        }

        // use a temp folder to clone so we don't leave a half-baked repo behind
        Path tempDir = targetDir.getParent()
                                .resolve(targetDir.getFileName() + "_temp_" + System.currentTimeMillis());
        Files.createDirectories(tempDir);
        logger.info("Cloning {} into {}", repoUrl, tempDir);

        String token = dotenv.get("GITHUB_TOKEN");
        Git git = Git.cloneRepository()
                     .setURI(repoUrl)
                     .setDirectory(tempDir.toFile())
                     .setCredentialsProvider(
                         (token != null && !token.isBlank())
                             ? new UsernamePasswordCredentialsProvider(token, "")
                             : null
                     )
                     .call();
        git.close();

        // move from tempDir → targetDir
        Files.createDirectories(targetDir);
        Files.walk(tempDir)
             .filter(p -> !p.equals(tempDir))
             .forEach(source -> {
                 try {
                     Path dest = targetDir.resolve(tempDir.relativize(source));
                     if (Files.isDirectory(source)) {
                         Files.createDirectories(dest);
                     } else {
                         Files.createDirectories(dest.getParent());
                         Files.move(source, dest);
                     }
                 } catch (IOException ex) {
                     logger.error("Error moving {} → {}", source, targetDir.resolve(tempDir.relativize(source)), ex);
                 }
             });

        // cleanup
        cleanupDirectory(tempDir);

        if (!Files.exists(targetDir.resolve(".git"))) {
            throw new IOException("Clone failed: .git folder not found in " + targetDir);
        }
        logger.info("Repository ready at {}", targetDir);
        return targetDir;
    }

    /**
     * Recursively delete a directory.
     */
    public void cleanupDirectory(Path dir) {
        try {
            if (Files.exists(dir)) {
                Files.walk(dir)
                     .sorted(Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(File::delete);
            }
        } catch (IOException e) {
            logger.warn("Failed to delete {}: {}", dir, e.getMessage());
        }
    }
}
