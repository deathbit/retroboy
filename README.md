# Retroboy

Retro collection builder - A Spring Boot application.

## 功能特性

### 启动时执行一次性代码

本应用使用 `StartupRunner` 组件在 Spring 应用启动时执行一次性初始化代码。

#### 使用方法

在 `src/main/java/com/github/deathbit/retroboy/StartupRunner.java` 文件中，找到 `run()` 方法，在其中添加您需要在应用启动时执行的代码：

```java
@Override
public void run(ApplicationArguments args) throws Exception {
    System.out.println("=== 执行一次性启动代码 ===");
    System.out.println("应用程序启动完成，开始执行初始化逻辑...");
    
    // 在这里添加需要在启动时执行的一次性代码
    // 例如：
    // - 初始化数据
    // - 加载配置
    // - 预热缓存
    // - 建立连接
    
    System.out.println("初始化完成！");
}
```

#### 实现原理

- 使用 Spring Boot 的 `ApplicationRunner` 接口
- 在 Spring 应用上下文完全初始化后自动执行
- 所有 Spring Bean 都已就绪，可以安全使用

## 构建和运行

### 编译项目

```bash
./mvnw clean compile
```

### 运行应用

```bash
./mvnw spring-boot:run
```

### 运行测试

```bash
./mvnw test
```

## 技术栈

- Spring Boot 4.0.2
- Java 17
- Maven
- Lombok
