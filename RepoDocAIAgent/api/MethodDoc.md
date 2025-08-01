```markdown
# Class Documentation: `MethodDoc`

## Overview
The `MethodDoc` class, part of the `com.documentor.agent.model` package, represents the documentation details of a method in a Java class. It encapsulates metadata about a method, including its name, description, return type, parameters, exceptions, annotations, and other relevant attributes. This class is designed to facilitate the generation and management of method-level documentation in Java projects.

## Class Hierarchy
- **Package**: `com.documentor.agent.model`
- **Class**: `MethodDoc`
  - **Type**: Concrete class
  - **Inheritance**: None (does not extend any class)
  - **Implemented Interfaces**: None

## Constructor Documentation
The `MethodDoc` class uses the Lombok annotations `@Data` and `@Builder`, which automatically generate constructors, getters, setters, and other utility methods. The primary way to create an instance of this class is through the builder pattern provided by Lombok.

### Example: Creating an Instance
```java
MethodDoc methodDoc = MethodDoc.builder()
    .name("calculateSum")
    .description("Calculates the sum of two integers.")
    .returnType("int")
    .returnDescription("The sum of the two integers.")
    .parameters(List.of(new ParameterDoc("a", "int", "First integer"),
                        new ParameterDoc("b", "int", "Second integer")))
    .exceptions(List.of("IllegalArgumentException"))
    .exceptionDescriptions(Map.of("IllegalArgumentException", "Thrown if input values are invalid."))
    .signature("public int calculateSum(int a, int b)")
    .annotations(Map.of("Override", "Indicates that this method overrides a superclass method."))
    .isPublic(true)
    .isStatic(false)
    .isAbstract(false)
    .sourceCode("public int calculateSum(int a, int b) { return a + b; }")
    .typeParameters(List.of())
    .codeExample("int result = calculateSum(5, 10);")
    .build();
```

## Field Documentation
The `MethodDoc` class contains the following fields:

### `name`
- **Type**: `String`
- **Description**: The name of the method.
- **Example**: `"calculateSum"`

### `description`
- **Type**: `String`
- **Description**: A brief description of the method's functionality.
- **Example**: `"Calculates the sum of two integers."`

### `returnType`
- **Type**: `String`
- **Description**: The return type of the method.
- **Example**: `"int"`

### `returnDescription`
- **Type**: `String`
- **Description**: A description of the method's return value.
- **Example**: `"The sum of the two integers."`

### `parameters`
- **Type**: `List<ParameterDoc>`
- **Description**: A list of parameters accepted by the method, represented as `ParameterDoc` objects.
- **Example**: 
  ```java
  List.of(new ParameterDoc("a", "int", "First integer"),
          new ParameterDoc("b", "int", "Second integer"))
  ```

### `exceptions`
- **Type**: `List<String>`
- **Description**: A list of exceptions that the method may throw.
- **Example**: `List.of("IllegalArgumentException")`

### `exceptionDescriptions`
- **Type**: `Map<String, String>`
- **Description**: A mapping of exception names to their descriptions.
- **Example**: 
  ```java
  Map.of("IllegalArgumentException", "Thrown if input values are invalid.")
  ```

### `signature`
- **Type**: `String`
- **Description**: The method's signature, including visibility, return type, name, and parameters.
- **Example**: `"public int calculateSum(int a, int b)"`

### `annotations`
- **Type**: `Map<String, String>`
- **Description**: A mapping of annotation names to their descriptions.
- **Example**: 
  ```java
  Map.of("Override", "Indicates that this method overrides a superclass method.")
  ```

### `isPublic`
- **Type**: `boolean`
- **Description**: Indicates whether the method is public.
- **Example**: `true`

### `isStatic`
- **Type**: `boolean`
- **Description**: Indicates whether the method is static.
- **Example**: `false`

### `isAbstract`
- **Type**: `boolean`
- **Description**: Indicates whether the method is abstract.
- **Example**: `false`

### `sourceCode`
- **Type**: `String`
- **Description**: The actual source code of the method.
- **Example**: `"public int calculateSum(int a, int b) { return a + b; }"`

### `typeParameters`
- **Type**: `List<String>`
- **Description**: A list of type parameters for generic methods.
- **Example**: `List.of("T")`

### `codeExample`
- **Type**: `String`
- **Description**: A code example demonstrating the usage of the method.
- **Example**: `"int result = calculateSum(5, 10);"`

## Method Documentation
The `MethodDoc` class does not define any explicit methods. All functionality is derived from the fields and the Lombok-generated methods.

## Usage Patterns and Best Practices
1. **Use the Builder Pattern**: Construct instances of `MethodDoc` using the builder pattern for clarity and maintainability.
2. **Document Methods Effectively**: Populate all fields to ensure comprehensive documentation of methods.
3. **Integrate with Other Classes**: Use `ParameterDoc` for parameter details and ensure exception descriptions are meaningful.
4. **Leverage Annotations**: Include annotations and their descriptions to provide additional context about the method.

### Example: Documenting a Method
```java
MethodDoc methodDoc = MethodDoc.builder()
    .name("divide")
    .description("Divides two integers and returns the result.")
    .returnType("double")
    .returnDescription("The result of the division.")
    .parameters(List.of(new ParameterDoc("numerator", "int", "The numerator."),
                        new ParameterDoc("denominator", "int", "The denominator.")))
    .exceptions(List.of("ArithmeticException"))
    .exceptionDescriptions(Map.of("ArithmeticException", "Thrown if the denominator is zero."))
    .signature("public double divide(int numerator, int denominator)")
    .annotations(Map.of())
    .isPublic(true)
    .isStatic(false)
    .isAbstract(false)
    .sourceCode("public double divide(int numerator, int denominator) { return numerator / (double) denominator; }")
    .typeParameters(List.of())
    .codeExample("double result = divide(10, 2);")
    .build();
```

## Related Classes and Dependencies
- **`ParameterDoc`**: Represents the details of a method parameter, including its name, type, and description.
- **`List`**: Used for storing collections of parameters, exceptions, and type parameters.
- **`Map`**: Used for storing mappings of exception names to descriptions and annotations to their descriptions.

## Conclusion
The `MethodDoc` class is a powerful tool for representing and managing method-level documentation in Java projects. By leveraging its fields and builder pattern, developers can create detailed and structured documentation for methods, facilitating better code understanding and maintenance.
```