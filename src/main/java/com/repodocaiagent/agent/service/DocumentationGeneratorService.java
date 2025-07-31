package com.repodocaiagent.agent.service;

import com.repodocaiagent.agent.model.JavaClassDoc;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for generating documentation using the Azure OpenAI LLM.
 */
public class DocumentationGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentationGeneratorService.class);
    
    private final ChatLanguageModel chatModel;

    public DocumentationGeneratorService(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }
    
    /**
     * Loads a prompt template from the resources directory.
     */
    private String loadPromptTemplate(String templateName) {
        try {
            return Files.readString(Path.of("src/main/resources/prompts/" + templateName));
        } catch (IOException e) {
            logger.warn("Failed to load prompt template {}, using fallback", templateName);
            return getFallbackPrompt(templateName);
        }
    }
    
    /**
     * Provides fallback prompts if template files cannot be loaded.
     */
    private String getFallbackPrompt(String templateName) {
        return switch (templateName) {
            case "project-overview.md" -> "Generate a README.md for {{repositoryName}} with {{totalClasses}} classes.";
            case "class-documentation.md" -> "Document the class {{className}} from package {{packageName}}.";
            case "getting-started.md" -> "Create a getting started guide for {{repositoryName}}.";
            case "faq-troubleshooting.md" -> "Create FAQ for {{repositoryName}}.";
            default -> "Generate documentation for {{repositoryName}}.";
        };
    }
    
    /**
     * Generates project overview documentation.
     * 
     * @param classes List of parsed Java classes
     * @param repositoryName Name of the GitHub repository
     * @param outputBasePath Base path to write generated documentation
     * @return Path to the generated README.md file
     * @throws IOException If an error occurs during file writing
     */
    public Path generateProjectOverview(List<JavaClassDoc> classes, String repositoryName, Path outputBasePath) throws IOException {
        logger.info("Generating project overview documentation for {}", repositoryName);
        
        // Enhanced analysis for better prompts
        String classSummary = classes.stream()
            .map(c -> String.format("- %s (%s): %s", 
                c.getFullyQualifiedName(), 
                c.getType(),
                c.getDescription() != null ? c.getDescription() : "No description available"))
            .collect(Collectors.joining("\n"));
        
        // Package structure analysis
        String packageStructure = classes.stream()
            .collect(Collectors.groupingBy(JavaClassDoc::getPackageName))
            .entrySet().stream()
            .map(entry -> String.format("- %s (%d classes)", 
                entry.getKey().isEmpty() ? "default package" : entry.getKey(), 
                entry.getValue().size()))
            .collect(Collectors.joining("\n"));
        
        // Identify main classes (those with main methods or public APIs)
        String mainClasses = classes.stream()
            .filter(c -> c.getMethods().stream()
                .anyMatch(m -> "main".equals(m.getName()) && m.isStatic()))
            .map(JavaClassDoc::getFullyQualifiedName)
            .collect(Collectors.joining("\n"));
        
        if (mainClasses.isEmpty()) {
            mainClasses = classes.stream()
                .filter(JavaClassDoc::isPublic)
                .limit(5)
                .map(JavaClassDoc::getFullyQualifiedName)
                .collect(Collectors.joining("\n"));
        }
        
        // Count different types of classes
        long interfaceCount = classes.stream().filter(c -> "INTERFACE".equals(c.getType())).count();
        long classCount = classes.stream().filter(c -> "CLASS".equals(c.getType())).count();
        long enumCount = classes.stream().filter(c -> "ENUM".equals(c.getType())).count();
        
        // Load prompt template from file
        String projectOverviewTemplate = loadPromptTemplate("project-overview.md");
        
        PromptTemplate template = PromptTemplate.from(projectOverviewTemplate);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("repositoryName", repositoryName);
        variables.put("totalClasses", classes.size());
        variables.put("classCount", classCount);
        variables.put("interfaceCount", interfaceCount);
        variables.put("enumCount", enumCount);
        variables.put("classSummary", classSummary);
        variables.put("packageStructure", packageStructure);
        variables.put("mainClasses", mainClasses.isEmpty() ? "No main classes identified" : mainClasses);
        
        Prompt prompt = template.apply(variables);
        
        // Generate content using the LLM
        String readmeContent = chatModel.generate(prompt.text());
        
        // Ensure output directory exists
        Files.createDirectories(outputBasePath);
        
        // Write the README.md file
        Path readmePath = outputBasePath.resolve("README.md");
        Files.writeString(readmePath, readmeContent);
        
        logger.info("Generated project overview at {}", readmePath);
        return readmePath;
    }
    
    /**
     * Generates detailed API documentation for a Java class.
     * 
     * @param classDoc The Java class to document
     * @param outputBasePath Base path to write generated documentation
     * @return Path to the generated class documentation file
     * @throws IOException If an error occurs during file writing
     */
    public Path generateClassDocumentation(JavaClassDoc classDoc, Path outputBasePath) throws IOException {
        logger.info("Generating documentation for class: {}", classDoc.getFullyQualifiedName());
        
        // Detailed methods analysis
        String methodsDetails = (classDoc.getMethods() != null) ? 
            classDoc.getMethods().stream()
                .map(m -> {
                    String params = (m.getParameters() != null) ? 
                        m.getParameters().stream()
                            .map(p -> p.getType() + " " + p.getName())
                            .collect(Collectors.joining(", ")) :
                        "";
                    String annotations = (m.getAnnotations() != null && !m.getAnnotations().isEmpty()) ? 
                        " [Annotations: " + String.join(", ", m.getAnnotations().keySet()) + "]" : "";
                    String visibility = m.isPublic() ? "public" : "private/protected";
                    String modifiers = "";
                    if (m.isStatic()) modifiers += "static ";
                    if (m.isAbstract()) modifiers += "abstract ";
                    
                    return String.format("- %s %s%s %s(%s)%s%s", 
                        visibility, 
                        modifiers, 
                        m.getReturnType() != null ? m.getReturnType() : "void", 
                        m.getName(), 
                        params,
                        annotations,
                        m.getDescription() != null ? ": " + m.getDescription() : "");
                })
                .collect(Collectors.joining("\n")) :
            "No methods available";
        
        // Detailed fields analysis  
        String fieldsDetails = (classDoc.getFields() != null) ?
            classDoc.getFields().stream()
                .map(f -> {
                    String annotations = (f.getAnnotations() != null && !f.getAnnotations().isEmpty()) ? 
                        " [Annotations: " + String.join(", ", f.getAnnotations().keySet()) + "]" : "";
                    String visibility = f.isPublic() ? "public" : "private/protected";
                    String modifiers = "";
                    if (f.isStatic()) modifiers += "static ";
                    if (f.isFinal()) modifiers += "final ";
                    
                    return String.format("- %s %s%s %s%s%s", 
                        visibility, 
                        modifiers, 
                        f.getType() != null ? f.getType() : "unknown", 
                        f.getName(),
                        annotations,
                        f.getDescription() != null ? ": " + f.getDescription() : "");
                })
                .collect(Collectors.joining("\n")) :
            "No fields available";
        
        // Constructor analysis - look for methods with same name as class
        String constructorsDetails = (classDoc.getMethods() != null) ?
            classDoc.getMethods().stream()
                .filter(m -> m.getName().equals(classDoc.getName()))
                .map(m -> {
                    String params = (m.getParameters() != null) ? 
                        m.getParameters().stream()
                            .map(p -> p.getType() + " " + p.getName())
                            .collect(Collectors.joining(", ")) :
                        "";
                    String visibility = m.isPublic() ? "public" : "private/protected";
                    return String.format("- %s %s(%s)%s", 
                        visibility, 
                        m.getName(), 
                        params,
                        m.getDescription() != null ? ": " + m.getDescription() : "");
                })
                .collect(Collectors.joining("\n")) :
            "";
        
        // Annotations analysis
        String classAnnotations = (classDoc.getAnnotations() != null && !classDoc.getAnnotations().isEmpty()) ? 
            String.join(", ", classDoc.getAnnotations().keySet()) : "None";
        
        // Inheritance analysis
        String inheritance = "";
        if (classDoc.getSuperClass() != null && !classDoc.getSuperClass().equals("Object")) {
            inheritance += "Extends: " + classDoc.getSuperClass();
        }
        if (classDoc.getImplementedInterfaces() != null && !classDoc.getImplementedInterfaces().isEmpty()) {
            if (!inheritance.isEmpty()) inheritance += "\n";
            inheritance += "Implements: " + String.join(", ", classDoc.getImplementedInterfaces());
        }
        if (inheritance.isEmpty()) {
            inheritance = "No explicit inheritance";
        }
        
        // Usage patterns analysis
        String usagePatterns = "";
        if (classDoc.getMethods() != null && classDoc.getMethods().stream().anyMatch(m -> "main".equals(m.getName()) && m.isStatic())) {
            usagePatterns += "- Entry point class (contains main method)\n";
        }
        if (classDoc.getType().equals("INTERFACE")) {
            usagePatterns += "- Interface definition\n";
        }
        if (classDoc.getAnnotations() != null && (classDoc.getAnnotations().containsKey("@Service") || classDoc.getAnnotations().containsKey("@Component"))) {
            usagePatterns += "- Spring service/component\n";
        }
        if (classDoc.getAnnotations() != null && (classDoc.getAnnotations().containsKey("@RestController") || classDoc.getAnnotations().containsKey("@Controller"))) {
            usagePatterns += "- Web controller\n";
        }
        if (classDoc.getAnnotations() != null && (classDoc.getAnnotations().containsKey("@Entity") || classDoc.getAnnotations().containsKey("@Table"))) {
            usagePatterns += "- JPA entity/data model\n";
        }
        if (usagePatterns.isEmpty()) {
            usagePatterns = "Standard Java class";
        }
        
        // Load prompt template from file
        String classDocTemplate = loadPromptTemplate("class-documentation.md");
        
        PromptTemplate template = PromptTemplate.from(classDocTemplate);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("className", classDoc.getName());
        variables.put("fullyQualifiedName", classDoc.getFullyQualifiedName());
        variables.put("packageName", classDoc.getPackageName());
        variables.put("classType", classDoc.getType());
        variables.put("isPublic", classDoc.isPublic());
        variables.put("isAbstract", classDoc.isAbstract());
        variables.put("implementedInterfaces", classDoc.getImplementedInterfaces() != null ? 
            String.join(", ", classDoc.getImplementedInterfaces()) : "None");
        variables.put("extendedClasses", classDoc.getSuperClass() != null ? classDoc.getSuperClass() : "None");
        variables.put("sourceCode", classDoc.getSourceCode() != null ? classDoc.getSourceCode() : "Source code not available");
        variables.put("classDescription", classDoc.getDescription() != null ? classDoc.getDescription() : "No description available");
        variables.put("methodsCount", classDoc.getMethods() != null ? classDoc.getMethods().size() : 0);
        variables.put("methodsDetails", methodsDetails.isEmpty() ? "No methods defined" : methodsDetails);
        variables.put("fieldsCount", classDoc.getFields() != null ? classDoc.getFields().size() : 0);
        variables.put("fieldsDetails", fieldsDetails.isEmpty() ? "No fields defined" : fieldsDetails);
        variables.put("constructorsDetails", constructorsDetails.isEmpty() ? "Default constructor" : constructorsDetails);
        variables.put("classAnnotations", classAnnotations);
        variables.put("inheritance", inheritance);
        variables.put("usagePatterns", usagePatterns);
        
        Prompt prompt = template.apply(variables);
        
        // Generate content using the LLM
        String documentation = chatModel.generate(prompt.text());
        
        // Create directory structure for API documentation
        Files.createDirectories(outputBasePath);
        
        // Create file name based on class name
        String fileName = classDoc.getName() + ".md";
        Path docPath = outputBasePath.resolve(fileName);
        
        Files.writeString(docPath, documentation);
        
        logger.info("Generated class documentation at {}", docPath);
        return docPath;
    }
    
    /**
     * Generates a getting started guide for the project.
     * 
     * @param classes List of parsed Java classes
     * @param repositoryName Name of the GitHub repository
     * @param outputBasePath Base path to write generated documentation
     * @return Path to the generated getting-started.md file
     * @throws IOException If an error occurs during file writing
     */
    public Path generateGettingStartedGuide(List<JavaClassDoc> classes, String repositoryName, Path outputBasePath) throws IOException {
        logger.info("Generating getting started guide for {}", repositoryName);
        
        // Find entry point classes (those with main methods)
        String entryPointClasses = classes.stream()
            .filter(c -> c.getMethods().stream()
                .anyMatch(m -> "main".equals(m.getName()) && m.isStatic()))
            .map(JavaClassDoc::getFullyQualifiedName)
            .collect(Collectors.joining("\n"));
        
        if (entryPointClasses.isEmpty()) {
            entryPointClasses = "No main methods found";
        }
        
        // Find important public classes
        String importantClasses = classes.stream()
            .filter(JavaClassDoc::isPublic)
            .filter(c -> !c.getType().equals("ENUM"))  // Skip enums for simplicity
            .limit(10)
            .map(c -> String.format("- %s (%s): %s", 
                c.getName(),
                c.getType().toLowerCase(),
                c.getDescription() != null ? c.getDescription() : "No description available"))
            .collect(Collectors.joining("\n"));
        
        // Analyze dependencies for setup instructions
        String dependencies = classes.stream()
            .filter(c -> c.getDependencies() != null)  // Add null check
            .flatMap(c -> c.getDependencies().stream())
            .distinct()
            .filter(dep -> !dep.startsWith("java."))  // Filter out standard Java classes
            .limit(15)
            .collect(Collectors.joining("\n"));
        
        // Package structure for understanding project layout
        String packageStructure = classes.stream()
            .collect(Collectors.groupingBy(JavaClassDoc::getPackageName))
            .entrySet().stream()
            .map(entry -> String.format("- %s (%d classes)", 
                entry.getKey().isEmpty() ? "default package" : entry.getKey(), 
                entry.getValue().size()))
            .collect(Collectors.joining("\n"));
        
        // Count different types of classes for project characterization
        long classCount = classes.stream().filter(c -> "CLASS".equals(c.getType())).count();
        long interfaceCount = classes.stream().filter(c -> "INTERFACE".equals(c.getType())).count();
        long enumCount = classes.stream().filter(c -> "ENUM".equals(c.getType())).count();
        
        // Load prompt template from file
        String gettingStartedTemplate = loadPromptTemplate("getting-started.md");
        
        PromptTemplate template = PromptTemplate.from(gettingStartedTemplate);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("repositoryName", repositoryName);
        variables.put("totalClasses", classes.size());
        variables.put("classCount", classCount);
        variables.put("interfaceCount", interfaceCount);
        variables.put("enumCount", enumCount);
        variables.put("mainClasses", entryPointClasses);
        variables.put("publicClasses", importantClasses.isEmpty() ? "No public classes identified" : importantClasses);
        variables.put("dependencies", dependencies.isEmpty() ? "No external dependencies identified" : dependencies);
        variables.put("packageStructure", packageStructure);
        variables.put("classAnalysis", importantClasses.isEmpty() ? "No detailed class analysis available" : importantClasses);
        variables.put("entryPointAnalysis", entryPointClasses.equals("No main methods found") ? 
            "No main methods found. This appears to be a library project." : 
            "Entry points identified:\n" + entryPointClasses);
        
        Prompt prompt = template.apply(variables);
        
        // Generate content using the LLM
        String gettingStartedContent = chatModel.generate(prompt.text());
        
        // Write the getting-started.md file
        Path gettingStartedPath = outputBasePath.resolve("getting-started.md");
        Files.writeString(gettingStartedPath, gettingStartedContent);
        
        logger.info("Generated getting started guide at {}", gettingStartedPath);
        return gettingStartedPath;
    }
    
    /**
     * Generates an FAQ and troubleshooting guide.
     * 
     * @param classes List of parsed Java classes
     * @param repositoryName Name of the GitHub repository
     * @param outputBasePath Base path to write generated documentation
     * @return Path to the generated faq.md file
     * @throws IOException If an error occurs during file writing
     */
    public Path generateFaqAndTroubleshooting(List<JavaClassDoc> classes, String repositoryName, Path outputBasePath) throws IOException {
        logger.info("Generating FAQ and troubleshooting guide for {}", repositoryName);
        
        // Collect exception information from methods to identify potential issues
        String exceptionTypes = classes.stream()
            .flatMap(c -> c.getMethods().stream())
            .flatMap(m -> m.getExceptions() != null ? m.getExceptions().stream() : List.<String>of().stream())
            .distinct()
            .collect(Collectors.joining("\n"));
        
        // Find methods that throw exceptions with their context
        String exceptionMethods = classes.stream()
            .flatMap(c -> c.getMethods().stream())
            .filter(m -> m.getExceptions() != null && !m.getExceptions().isEmpty())
            .map(m -> String.format("- %s.%s() throws: %s", 
                m.getName().contains(".") ? m.getName().substring(0, m.getName().lastIndexOf('.')) : "Unknown",
                m.getName(),
                String.join(", ", m.getExceptions())))
            .collect(Collectors.joining("\n"));
        
        // Analyze common patterns that might cause issues
        String commonPatterns = "";
        
        // Check for file operations
        long fileOperationMethods = classes.stream()
            .flatMap(c -> c.getMethods().stream())
            .filter(m -> m.getReturnType().contains("Path") || 
                        m.getReturnType().contains("File") ||
                        (m.getParameters() != null && m.getParameters().stream().anyMatch(p -> p.getType().contains("Path") || p.getType().contains("File"))))
            .count();
        
        if (fileOperationMethods > 0) {
            commonPatterns += "- File I/O operations (" + fileOperationMethods + " methods)\n";
        }
        
        // Check for network operations  
        long networkMethods = classes.stream()
            .flatMap(c -> c.getMethods().stream())
            .filter(m -> m.getReturnType().contains("Http") || 
                        m.getReturnType().contains("URL") ||
                        m.getName().toLowerCase().contains("connect"))
            .count();
        
        if (networkMethods > 0) {
            commonPatterns += "- Network operations (" + networkMethods + " methods)\n";
        }
        
        // Check for configuration/properties
        long configMethods = classes.stream()
            .flatMap(c -> c.getMethods().stream())
            .filter(m -> m.getReturnType().contains("Properties") || 
                        m.getName().toLowerCase().contains("config"))
            .count();
        
        if (configMethods > 0) {
            commonPatterns += "- Configuration operations (" + configMethods + " methods)\n";
        }
        
        if (commonPatterns.isEmpty()) {
            commonPatterns = "Standard Java operations";
        }
        
        // Analyze dependencies for setup instructions  
        String dependencies = classes.stream()
            .filter(c -> c.getDependencies() != null)
            .flatMap(c -> c.getDependencies().stream())
            .distinct()
            .filter(dep -> !dep.startsWith("java."))
            .limit(10)
            .collect(Collectors.joining("\n"));
        
        // Analyze technology stack based on class patterns
        String technologyStack = "";
        if (classes.stream().anyMatch(c -> c.getAnnotations() != null && 
            (c.getAnnotations().containsKey("@RestController") || c.getAnnotations().containsKey("@Controller")))) {
            technologyStack += "Spring Boot Web, ";
        }
        if (classes.stream().anyMatch(c -> c.getAnnotations() != null && c.getAnnotations().containsKey("@Entity"))) {
            technologyStack += "JPA/Hibernate, ";
        }
        if (classes.stream().anyMatch(c -> c.getName().contains("Test"))) {
            technologyStack += "JUnit Testing, ";
        }
        if (technologyStack.isEmpty()) {
            technologyStack = "Core Java";
        } else {
            technologyStack = technologyStack.replaceAll(", $", ""); // Remove trailing comma
        }
        
        // Identify complex classes (those with many methods or complex inheritance)
        String complexClasses = classes.stream()
            .filter(c -> (c.getMethods() != null && c.getMethods().size() > 10) || 
                        (c.getImplementedInterfaces() != null && !c.getImplementedInterfaces().isEmpty()))
            .limit(5)
            .map(c -> String.format("- %s (%d methods)", c.getName(), 
                c.getMethods() != null ? c.getMethods().size() : 0))
            .collect(Collectors.joining("\n"));
        
        if (complexClasses.isEmpty()) {
            complexClasses = "No particularly complex classes identified";
        }
        
        // Potential issues based on patterns
        String potentialIssues = "";
        if (fileOperationMethods > 0) {
            potentialIssues += "- File I/O operations may cause permission or path issues\n";
        }
        if (networkMethods > 0) {
            potentialIssues += "- Network connectivity and timeout issues\n";
        }
        if (configMethods > 0) {
            potentialIssues += "- Configuration and properties setup issues\n";
        }
        if (potentialIssues.isEmpty()) {
            potentialIssues = "Standard Java runtime issues";
        }
        
        // Usage patterns for FAQ
        String usagePatterns = classes.stream()
            .filter(c -> c.isPublic() && c.getMethods() != null)
            .filter(c -> c.getMethods().stream().anyMatch(m -> m.isPublic()))
            .limit(3)
            .map(c -> String.format("- %s: Primary public API class", c.getName()))
            .collect(Collectors.joining("\n"));
        
        if (usagePatterns.isEmpty()) {
            usagePatterns = "Standard library usage patterns";
        }
        
        // Load prompt template from file
        String faqTemplate = loadPromptTemplate("faq-troubleshooting.md");
        
        PromptTemplate template = PromptTemplate.from(faqTemplate);
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("repositoryName", repositoryName);
        variables.put("totalClasses", classes.size());
        variables.put("technologyStack", technologyStack);
        variables.put("commonPatterns", commonPatterns);
        variables.put("complexClasses", complexClasses);
        variables.put("dependencies", dependencies.isEmpty() ? "No external dependencies identified" : dependencies);
        variables.put("potentialIssues", potentialIssues);
        variables.put("usagePatterns", usagePatterns);
        variables.put("exceptionTypes", exceptionTypes.isEmpty() ? "No exceptions declared" : exceptionTypes);
        variables.put("exceptionMethods", exceptionMethods.isEmpty() ? "No methods with declared exceptions" : exceptionMethods);
        
        Prompt prompt = template.apply(variables);
        
        // Generate content using the LLM
        String faqContent = chatModel.generate(prompt.text());
        
        // Write the faq.md file
        Path faqPath = outputBasePath.resolve("faq.md");
        Files.writeString(faqPath, faqContent);
        
        logger.info("Generated FAQ and troubleshooting guide at {}", faqPath);
        return faqPath;
    }
}
