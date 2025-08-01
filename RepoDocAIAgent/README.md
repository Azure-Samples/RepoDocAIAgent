```markdown
# AI Agents for Java

AI Agents for Java is an advanced tool designed to autonomously generate high-quality documentation for Java repositories. Leveraging LangChain4J and Azure OpenAI, this project simplifies the process of documenting Java codebases by parsing source files, extracting relevant information, and generating detailed documentation for classes, methods, fields, and parameters.

## Key Features

- **Autonomous Documentation Generation**: Automatically generates comprehensive documentation for Java repositories using AI-powered language models.
- **Java Code Parsing**: Extracts detailed information about classes, methods, fields, and parameters from Java source files.
- **Azure OpenAI Integration**: Utilizes Azure OpenAI's large language models for generating human-readable documentation.
- **GitHub Repository Support**: Handles operations related to GitHub repositories, enabling seamless integration with hosted codebases.
- **Modular Design**: Organized into distinct services and models for scalability and maintainability.

## Architecture Overview

The repository is structured into three main packages:

1. **com.repodocaiagent.agent**:
   - Contains the main application class, `repodocaiagentApplication`, which serves as the entry point for the tool.

2. **com.repodocaiagent.agent.model**:
   - Defines the core data models used for documentation generation:
     - `FieldDoc`: Represents documentation for a field in a Java class.
     - `JavaClassDoc`: Represents a Java class or interface with its documentation details.
     - `MethodDoc`: Represents a method in a Java class with its documentation details.
     - `ParameterDoc`: Represents a method parameter with its documentation details.

3. **com.repodocaiagent.agent.service**:
   - Implements the core services for functionality:
     - `AzureOpenAiService`: Configures and provides access to Azure OpenAI's language models.
     - `GitHubService`: Handles GitHub repository operations.
     - `JavaParserService`: Parses Java source files and extracts relevant documentation data.

## Installation and Setup

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- Maven 3.6 or higher
- Azure OpenAI API key
- GitHub Personal Access Token (if using GitHub integration)

### Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/AI-Agents-for-Java.git
   cd AI-Agents-for-Java
   ```

2. Build the project using Maven:
   ```bash
   mvn clean install
   ```

3. Configure environment variables:
   - `AZURE_OPENAI_API_KEY`: Your Azure OpenAI API key.
   - `GITHUB_TOKEN`: Your GitHub Personal Access Token (optional, for GitHub integration).

4. Run the application:
   ```bash
   java -jar target/AI-Agents-for-Java-1.0.jar
   ```

## Usage Examples

### Generating Documentation for a Local Java Repository

1. Place your Java source files in a directory.
2. Run the application with the directory path as an argument:
   ```bash
   java -jar target/AI-Agents-for-Java-1.0.jar /path/to/java/repository
   ```
3. The tool will parse the source files and generate documentation in the output directory.

### Generating Documentation for a GitHub Repository

1. Provide the GitHub repository URL as an argument:
   ```bash
   java -jar target/AI-Agents-for-Java-1.0.jar https://github.com/username/repository-name
   ```
2. The tool will clone the repository, parse the source files, and generate documentation.

### Example Output

Generated documentation includes:
- Class-level descriptions
- Method summaries with parameter details
- Field-level documentation

Example:
```markdown
# Class: com.example.MyClass

## Fields
- `private int id`: Represents the unique identifier for the object.

## Methods
- `public String getName()`: Retrieves the name of the object.
  - Parameters: None
  - Returns: The name as a `String`.

- `public void setName(String name)`: Sets the name of the object.
  - Parameters:
    - `name`: The new name to set.
  - Returns: None
```

## API Reference

For detailed API documentation, refer to the generated documentation files or integrate the tool with your IDE for inline documentation.

## Contributing

We welcome contributions to improve AI Agents for Java. To contribute:

1. Fork the repository.
2. Create a feature branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. Commit your changes and push to your forked repository.
4. Submit a pull request with a detailed description of your changes.

### Guidelines

- Follow Java best practices and coding standards.
- Write unit tests for new features.
- Ensure compatibility with JDK 11 or higher.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

Start automating your Java documentation process today with AI Agents for Java!
```