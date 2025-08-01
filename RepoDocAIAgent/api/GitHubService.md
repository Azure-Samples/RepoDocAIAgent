
# GitHubService Class Documentation

## Overview
The `GitHubService` class, part of the `com.repodocaiagent.agent.service` package, provides functionality for interacting with GitHub repositories. It allows users to clone repositories to a local directory, clean up temporary directories, and extract repository names from GitHub URLs. This service is designed to handle both public and private repositories, leveraging authentication via a GitHub token when necessary.

## Class Hierarchy
- **Package**: `com.repodocaiagent.agent.service`
- **Class**: `GitHubService`
  - **Type**: Concrete class (not abstract)
  - **Inheritance**: None (does not extend any class)
  - **Implemented Interfaces**: None

## Constructor
### `GitHubService(Dotenv dotenv)`
Creates an instance of the `GitHubService` class.

#### Parameters:
- `dotenv` (`Dotenv`): A configuration object used to retrieve environment variables, such as the GitHub token for authentication.

#### Example:
```java
Dotenv dotenv = Dotenv.configure().load();
GitHubService gitHubService = new GitHubService(dotenv);
```

---

## Fields
### `private static final Logger logger`
A logger instance used for logging messages and errors during GitHub repository operations.

### `private final Dotenv dotenv`
A `Dotenv` instance used to access environment variables, such as the GitHub token for authentication.

---

## Methods

### `public Path cloneRepository(String repoUrl)`
Clones a GitHub repository to a local directory. This method supports both public and private repositories, using a GitHub token for authentication when available.

#### Parameters:
- `repoUrl` (`String`): The URL of the GitHub repository to clone (e.g., `"https://github.com/username/repo.git"`).

#### Returns:
- `Path`: The path to the local directory where the repository was cloned.

#### Exceptions:
- `GitAPIException`: Thrown if there is an error during Git operations.
- `IOException`: Thrown if there is an error creating directories or accessing files.

#### Example:
```java
try {
    Dotenv dotenv = Dotenv.configure().load();
    GitHubService gitHubService = new GitHubService(dotenv);
    Path clonedRepoPath = gitHubService.cloneRepository("https://github.com/username/repo.git");
    System.out.println("Repository cloned to: " + clonedRepoPath);
} catch (GitAPIException | IOException e) {
    e.printStackTrace();
}
```

---

### `public void cleanupDirectory(Path directory)`
Deletes all files and subdirectories within the specified directory. This method is useful for cleaning up temporary directories created during repository operations.

#### Parameters:
- `directory` (`Path`): The path to the directory to clean up.

#### Returns:
- `void`: This method does not return a value.

#### Example:
```java
Dotenv dotenv = Dotenv.configure().load();
GitHubService gitHubService = new GitHubService(dotenv);
Path tempDirectory = Path.of("c:\\githublocal\\tempRepo");
gitHubService.cleanupDirectory(tempDirectory);
System.out.println("Temporary directory cleaned up.");
```

---

### `private String extractRepositoryName(String repoUrl)`
Extracts the repository name from a GitHub URL. The method handles various URL formats and removes the `.git` extension if present.

#### Parameters:
- `repoUrl` (`String`): The URL of the GitHub repository.

#### Returns:
- `String`: The name of the repository (e.g., `"repo"` for `"https://github.com/username/repo.git"`).

#### Example:
```java
Dotenv dotenv = Dotenv.configure().load();
GitHubService gitHubService = new GitHubService(dotenv);
String repoName = gitHubService.extractRepositoryName("https://github.com/username/repo.git");
System.out.println("Repository name: " + repoName);
```

---

## Usage Patterns and Best Practices
1. **Cloning Repositories**:
   - Use the `cloneRepository` method to clone public or private repositories.
   - Ensure the `Dotenv` instance contains the `GITHUB_TOKEN` environment variable for private repositories.

2. **Cleaning Up Temporary Directories**:
   - Use the `cleanupDirectory` method to delete temporary directories after repository operations.
   - Always verify the directory path before cleanup to avoid accidental deletion.

3. **Extracting Repository Names**:
   - Use the `extractRepositoryName` method to parse repository names from GitHub URLs for logging or directory naming purposes.

---

## Related Classes and Dependencies
- **Dotenv**:
  - Used to manage environment variables, including the GitHub token.
  - Import: `io.github.cdimascio.dotenv.Dotenv`

- **Git**:
  - Used for Git operations such as cloning repositories.
  - Import: `org.eclipse.jgit.api.Git`

- **Logger**:
  - Used for logging messages and errors.
  - Import: `org.slf4j.Logger`
  - Import: `org.slf4j.LoggerFactory`

---

## Example Workflow
```java
import io.github.cdimascio.dotenv.Dotenv;
import com.repodocaiagent.agent.service.GitHubService;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;

public class GitHubServiceExample {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        GitHubService gitHubService = new GitHubService(dotenv);

        try {
            // Clone a repository
            Path clonedRepoPath = gitHubService.cloneRepository("https://github.com/username/repo.git");
            System.out.println("Repository cloned to: " + clonedRepoPath);

            // Clean up the cloned repository
            gitHubService.cleanupDirectory(clonedRepoPath);
            System.out.println("Cloned repository cleaned up.");
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }
    }
}
```
```