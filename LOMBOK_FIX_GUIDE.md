# Lombok Configuration Fix Guide

## Issue
Maven is not processing Lombok annotations, causing compilation errors with "cannot find symbol" for getters/setters.

## Root Cause
Lombok annotation processor is not being invoked during Maven compilation.

## Solutions

### Solution 1: IDE Configuration (Recommended)

#### For IntelliJ IDEA:
1. Install Lombok Plugin:
   - Go to `File` → `Settings` → `Plugins`
   - Search for "Lombok"
   - Install the plugin
   - Restart IntelliJ IDEA

2. Enable Annotation Processing:
   - Go to `File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
   - Check "Enable annotation processing"
   - Click "Apply" and "OK"

3. Reimport Maven Project:
   - Right-click on `pom.xml`
   - Select `Maven` → `Reload Project`

4. Rebuild Project:
   - Go to `Build` → `Rebuild Project`

#### For Eclipse:
1. Install Lombok:
   - Download lombok.jar from https://projectlombok.org/download
   - Run: `java -jar lombok.jar`
   - Select your Eclipse installation
   - Click "Install/Update"
   - Restart Eclipse

2. Reimport Maven Project:
   - Right-click on project → `Maven` → `Update Project`

3. Clean and Build:
   - `Project` → `Clean`
   - `Project` → `Build Project`

### Solution 2: Maven Command Line Fix

Add this to your `pom.xml` in the `<build><plugins>` section:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>17</source>
        <target>17</target>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.30</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

Then run:
```bash
mvn clean install -U
```

### Solution 3: Verify Lombok Dependency

Ensure Lombok is in your `pom.xml`:

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### Solution 4: Use Spring Boot DevTools

Spring Boot DevTools can sometimes help with annotation processing:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

## Verification Steps

After applying the fix:

1. Clean the project:
   ```bash
   mvn clean
   ```

2. Compile:
   ```bash
   mvn compile
   ```

3. Check for errors - you should see:
   ```
   [INFO] BUILD SUCCESS
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

5. You should see:
   ```
   Started BudgetWiseApplication in X seconds
   ```

## Quick Test

Create a simple test class to verify Lombok is working:

```java
import lombok.Data;

@Data
public class TestLombok {
    private String name;
    private int age;
}
```

Try to compile it. If it works, Lombok is configured correctly.

## Alternative: Manual Getters/Setters

If Lombok continues to cause issues, you can temporarily remove `@Data` annotations and add manual getters/setters. However, this is not recommended as it defeats the purpose of using Lombok.

## Common Issues

### Issue: "cannot find symbol" errors
**Solution**: Enable annotation processing in IDE

### Issue: Lombok plugin not found
**Solution**: Update IDE to latest version, then install Lombok plugin

### Issue: Maven can't download Lombok
**Solution**: Check internet connection, clear Maven cache:
```bash
rm -rf ~/.m2/repository/org/projectlombok
mvn clean install -U
```

### Issue: Wrong Java version
**Solution**: Ensure you're using Java 17:
```bash
java -version
```

## Next Steps

Once Lombok is working:
1. ✅ Compile the project successfully
2. ✅ Run the backend server
3. ✅ Test endpoints in Postman using `TASK_5_6_7_TESTING_GUIDE.md`

## Support

If issues persist:
1. Check IDE logs for specific errors
2. Verify Java version is 17
3. Ensure Maven is using the correct Java version
4. Try running from command line instead of IDE
5. Check if antivirus is blocking annotation processing

---

**Note**: The code is correct. This is purely a build configuration issue that's common with Lombok in certain environments.
