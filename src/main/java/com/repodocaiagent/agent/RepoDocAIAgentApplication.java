// src/main/java/com/repodocaiagent/agent/RepoDocAIAgentApplication.java
package com.repodocaiagent.agent;

import com.repodocaiagent.agent.model.JavaClassDoc;
import com.repodocaiagent.agent.service.AzureOpenAiService;
import com.repodocaiagent.agent.service.DocumentationGeneratorService;
import com.repodocaiagent.agent.service.GitHubService;
import com.repodocaiagent.agent.service.JavaParserService;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main application class for the Java Documentation AI Agent.
 */
public class RepoDocAIAgentApplication {
    private static final Logger logger = LoggerFactory.getLogger(RepoDocAIAgentApplication.class);

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("Usage: java -jar java-documentation-agent.jar <github-repo-url>");
                System.exit(1);
            }
            String repoUrl = args[0];
            logger.info("Processing {}", repoUrl);

            // load env
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            String docDest = dotenv.get("documentdestination");
            if (docDest == null || docDest.isBlank()) {
                System.err.println("Please set documentdestination in your .env (e.g. documentdestination=C:/githublocal/RepoDocAIAgent)");
                System.exit(1);
            }

            Path baseDir = Path.of(docDest);                   // C:/githublocal/RepoDocAIAgent
            String repoName = extractRepoSimpleName(repoUrl);  // e.g. "azure-javaweb-app"
            Path targetRepo = baseDir.resolve(repoName);       // → C:/githublocal/RepoDocAIAgent/azure-javaweb-app

            // if it already exists, append timestamp
            if (Files.exists(targetRepo)) {
                String ts = String.valueOf(System.currentTimeMillis());
                targetRepo = baseDir.resolve(repoName + "-" + ts);
                logger.info("Target exists, using {}", targetRepo);
            }

            // initialize services
            GitHubService git = new GitHubService(dotenv);
            JavaParserService parser = new JavaParserService();
            DocumentationGeneratorService docs = new DocumentationGeneratorService(
                new AzureOpenAiService(dotenv).getChatModel()
            );

            // clone into azure-javaweb-app folder
            Path repoPath = git.cloneRepository(repoUrl, targetRepo)
                                .toAbsolutePath().normalize();

            // ─── Flatten a nested repo folder if present ───
            Path nested = repoPath.resolve(repoName);
            if (Files.exists(nested) && Files.isDirectory(nested)) {
                logger.info("Detected nested folder {}, flattening...", nested);
                Files.walk(nested)
                     .filter(p -> !p.equals(nested))
                     .forEach(source -> {
                         try {
                             Path dest = repoPath.resolve(nested.relativize(source));
                             if (Files.isDirectory(source)) {
                                 Files.createDirectories(dest);
                             } else {
                                 Files.createDirectories(dest.getParent());
                                 Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
                             }
                         } catch (IOException ex) {
                             logger.error("Error flattening {} → {}: {}", source, repoPath, ex.getMessage());
                         }
                     });
                // Delete the now-empty nested folder
                Files.walk(nested)
                     .sorted(Comparator.reverseOrder())
                     .map(Path::toFile)
                     .forEach(File::delete);
            }
            // ────────────────────────────────────────────────

            // find & parse Java files
            List<Path> javaFiles = parser.findJavaFiles(repoPath);
            List<JavaClassDoc> classes = new ArrayList<>();
            for (Path f : javaFiles) {
                JavaClassDoc cd = parser.parseJavaFile(f);
                if (cd != null) classes.add(cd);
            }

            // create RepoDocAIAgent/ and api/ under the repo folder
            Path docRoot = repoPath.resolve("RepoDocAIAgent");     // .../azure-javaweb-app/RepoDocAIAgent
            Path apiRoot = docRoot.resolve("api");
            Files.createDirectories(apiRoot);

            // generate documentation
            docs.generateProjectOverview(classes, repoName, docRoot);
            docs.generateGettingStartedGuide(classes, repoName, docRoot);
            docs.generateFaqAndTroubleshooting(classes, repoName, docRoot);
            for (JavaClassDoc cd : classes) {
                docs.generateClassDocumentation(cd, apiRoot);
            }

            logger.info("✅ Documentation written to {}", docRoot);
            System.out.println("View docs at: " + docRoot);
        }
        catch (Exception e) {
            logger.error("Failed", e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    /** helper to pull just the repo name (without owner) */
    private static String extractRepoSimpleName(String url) {
        Matcher m = Pattern.compile("github\\.com[/:]([^/]+)/([^/.]+)").matcher(url);
        if (m.find()) {
            return m.group(2);
        }
        String[] parts = url.split("/");
        String last = parts[parts.length - 1];
        return last.endsWith(".git")
             ? last.substring(0, last.length() - 4)
             : last;
    }
}
