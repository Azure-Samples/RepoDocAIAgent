
# Getting Started with AI-Agents-for-Java

Welcome to the **AI-Agents-for-Java** Getting Started guide! This guide will help you set up, configure, and use the AI Documentation Agent to autonomously generate high-quality documentation for Java repositories. By the end of this guide, you'll be able to parse Java source files, extract documentation details, and generate meaningful documentation using the provided APIs.

---

## Prerequisites

Before you begin, ensure you have the following:

1. **Java Development Kit (JDK)**: Version 11 or higher.
   - [Download JDK](https://www.oracle.com/java/technologies/javase-downloads.html)
2. **Maven**: For building and managing dependencies.
   - [Install Maven](https://maven.apache.org/install.html)
3. **Git**: To clone the repository.
   - [Install Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)
4. **Azure OpenAI API Key**: Required for accessing Azure OpenAI services.
   - [Get an API Key](https://learn.microsoft.com/en-us/azure/cognitive-services/openai/quickstart)
5. **GitHub Personal Access Token**: Required for interacting with GitHub repositories.
   - [Generate a Token](https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token)

---

## Installation Instructions

Follow these steps to set up the project:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-organization/AI-Agents-for-Java.git
   cd AI-Agents-for-Java
   ```

2. **Build the Project**:
   Use Maven to build the project:
   ```bash
   mvn clean install
   ```

3. **Configure Environment Variables**:
   Set the following environment variables for Azure OpenAI and GitHub integration:
   ```bash
   export AZURE_OPENAI_API_KEY=your-azure-api-key
   export GITHUB_PERSONAL_ACCESS_TOKEN=your-github-token
   ```

4. **Run the Application**:
   Start the main application:
   ```bash
   java -cp target/AI-Agents-for-Java-1.0-SNAPSHOT.jar com.repodocaiagent.agent.repodocaiagentApplication
   ```

---

## Quick Start Tutorial

This section provides a simple example to get you started with the AI Documentation Agent.

### Step 1: Parse a Java Repository

The `repodocaiagentApplication` class is the main entry point. It uses the `JavaParserService` to parse Java source files and generate documentation.

Here’s a basic example:

```java
import com.repodocaiagent.agent.repodocaiagentApplication;

public class Main {
    public static void main(String[] args) {
        // Path to the Java repository you want to document
        String repoPath = "/path/to/your/java/repository";

        // Start the documentation process
        repodocaiagentApplication repodocaiagent = new repodocaiagentApplication();
        repodocaiagent.generateDocumentation(repoPath);

        System.out.println("Documentation generation complete!");
    }
}
```

### Step 2: Run the Example

1. Save the code above as `Main.java`.
2. Compile and run the program:
   ```bash
   javac -cp target/AI-Agents-for-Java-1.0-SNAPSHOT.jar Main.java
   java -cp .:target/AI-Agents-for-Java-1.0-SNAPSHOT.jar Main
   ```

---

## Basic Usage Patterns

### Generate Documentation for a Specific Class

To document a specific Java class, use the `JavaParserService` directly:

```java
import com.repodocaiagent.agent.service.JavaParserService;
import com.repodocaiagent.agent.model.JavaClassDoc;

public class ClassDocumentationExample {
    public static void main(String[] args) {
        // Initialize the JavaParserService
        JavaParserService parserService = new JavaParserService();

        // Path to the Java file
        String javaFilePath = "/path/to/your/JavaClass.java";

        // Parse and document the class
        JavaClassDoc classDoc = parserService.parseJavaClass(javaFilePath);

        // Print the generated documentation
        System.out.println("Class Name: " + classDoc.getClassName());
        System.out.println("Class Description: " + classDoc.getDescription());
    }
}
```

---

## Common Use Cases

### 1. Documenting All Classes in a Repository

```java
import com.repodocaiagent.agent.service.JavaParserService;
import com.repodocaiagent.agent.model.JavaClassDoc;

import java.io.File;
import java.util.List;

public class RepositoryDocumentationExample {
    public static void main(String[] args) {
        // Initialize the JavaParserService
        JavaParserService parserService = new JavaParserService();

        // Path to the repository
        String repoPath = "/path/to/your/java/repository";

        // Parse and document all classes in the repository
        List<JavaClassDoc> classDocs = parserService.parseRepository(new File(repoPath));

        // Print documentation for each class
        for (JavaClassDoc classDoc : classDocs) {
            System.out.println("Class Name: " + classDoc.getClassName());
            System.out.println("Description: " + classDoc.getDescription());
            System.out.println("Methods: " + classDoc.getMethods());
            System.out.println("Fields: " + classDoc.getFields());
            System.out.println("=================================");
        }
    }
}
```

### 2. Using Azure OpenAI for Enhanced Documentation

```java
import com.repodocaiagent.agent.service.AzureOpenAiService;

public class OpenAiIntegrationExample {
    public static void main(String[] args) {
        // Initialize the AzureOpenAiService
        AzureOpenAiService openAiService = new AzureOpenAiService();

        // Example input for documentation enhancement
        String rawDocumentation = "This is a sample Java class.";

        // Enhance documentation using Azure OpenAI
        String enhancedDocumentation = openAiService.enhanceDocumentation(rawDocumentation);

        // Print the enhanced documentation
        System.out.println("Enhanced Documentation: " + enhancedDocumentation);
    }
}
```

---

## Next Steps and Advanced Features

1. **Integrate with GitHub**:
   Use the `GitHubService` to fetch repositories directly from GitHub and document them:
   ```java
   import com.repodocaiagent.agent.service.GitHubService;

   public class GitHubIntegrationExample {
       public static void main(String[] args) {
           // Initialize the GitHubService
           GitHubService gitHubService = new GitHubService();

           // Clone a repository
           String repoUrl = "https://github.com/your-username/your-repo.git";
           String localPath = "/path/to/local/repo";
           gitHubService.cloneRepository(repoUrl, localPath);

           System.out.println("Repository cloned successfully!");
       }
   }
   ```

2. **Customize Documentation Output**:
   Extend the `JavaClassDoc`, `FieldDoc`, and `MethodDoc` classes to include additional metadata or formatting.

3. **Automate Documentation Pipelines**:
   Combine the services to create a CI/CD pipeline for generating and publishing documentation.

---

## Troubleshooting Tips

- **Issue**: `java.lang.NoClassDefFoundError`
  - **Solution**: Ensure the classpath includes the compiled JAR file.

- **Issue**: `Invalid Azure API Key`
  - **Solution**: Verify the `AZURE_OPENAI_API_KEY` environment variable.

- **Issue**: `GitHub Authentication Failed`
  - **Solution**: Ensure the `GITHUB_PERSONAL_ACCESS_TOKEN` is valid and has the required permissions.

---

Congratulations! You are now ready to use the AI Documentation Agent to generate high-quality documentation for your Java projects. For more advanced usage, refer to the source code and extend the functionality as needed.
```