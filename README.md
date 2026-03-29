# Back Admin - Spring Boot 后台管理系统

基于 Spring Boot 2.7 + MyBatis Plus 的后台管理系统后端项目

## 技术栈

- **Spring Boot**: 2.7.18
- **MyBatis Plus**: 3.5.3.1
- **MySQL**: 8.0+
- **JWT**: 0.9.1 (用户认证)
- **Hutool**: 5.8.22 (工具类库)
- **Lombok**: 1.18.30

## 项目结构

```
back-admin/
├── src/main/java/com/example/admin/
│   ├── AdminApplication.java          # 启动类
│   ├── common/                        # 公共类
│   │   ├── Result.java               # 统一响应封装
│   │   └── PageResult.java           # 分页结果封装
│   ├── config/                        # 配置类
│   │   ├── CorsConfig.java           # 跨域配置
│   │   ├── GlobalExceptionHandler.java # 全局异常处理
│   │   ├── MybatisPlusConfig.java    # MyBatis Plus 配置
│   │   └── MybatisPlusInterceptorConfig.java # 分页插件配置
│   ├── controller/                    # 控制器层
│   │   └── UserController.java       # 用户控制器
│   ├── dto/                           # 数据传输对象
│   │   └── UserDTO.java              # 用户 DTO
│   ├── entity/                        # 实体类
│   │   └── User.java                 # 用户实体
│   ├── mapper/                        # Mapper 接口
│   │   └── UserMapper.java           # 用户 Mapper
│   └── service/                       # 服务层
│       ├── UserService.java          # 用户服务接口
│       └── impl/
│           └── UserServiceImpl.java  # 用户服务实现
├── src/main/resources/
│   ├── application.yml               # 应用配置文件
│   └── schema.sql                    # 数据库初始化脚本
└── pom.xml                           # Maven 配置文件
```

## 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库初始化

执行 `src/main/resources/schema.sql` 创建数据库和表：

```bash
mysql -u root -p < src/main/resources/schema.sql
```

或手动在 MySQL 中执行 SQL 脚本内容。

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/back_admin?...
    username: your_username
    password: your_password
```

### 4. 运行项目

```bash
mvn spring-boot:run
```

或使用 IDE 运行 `AdminApplication.java`

### 5. 访问接口

项目启动后访问：http://localhost:8080/api

## API 接口

### 用户管理

| 方法   | 路径            | 描述             |
| ------ | --------------- | ---------------- |
| POST   | /api/user/login | 用户登录         |
| GET    | /api/user/{id}  | 根据 ID 查询用户 |
| GET    | /api/user/page  | 分页查询用户列表 |
| GET    | /api/user/list  | 获取所有用户列表 |
| POST   | /api/user       | 创建用户         |
| PUT    | /api/user/{id}  | 更新用户         |
| DELETE | /api/user/{id}  | 删除用户         |

### 接口示例

#### 用户登录

```bash
POST http://localhost:8080/api/user/login?username=admin&password=admin123
```

响应：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": "eyJhbGciOiJIUzI1NiJ9...",
  "timestamp": 1234567890
}
```

#### 创建用户

```bash
POST http://localhost:8080/api/user
Content-Type: application/json

{
  "username": "test",
  "password": "test123",
  "nickname": "测试用户",
  "email": "test@example.com",
  "status": 1
}
```

#### 分页查询

```bash
GET http://localhost:8080/api/user/page?pageNum=1&pageSize=10
```

## 默认账户

- 用户名：`admin`
- 密码：`admin123`

## 功能特性

- ✅ 统一响应结果封装
- ✅ 全局异常处理
- ✅ JWT Token 认证
- ✅ 密码 BCrypt 加密
- ✅ MyBatis Plus 分页插件
- ✅ 自动填充创建/更新时间
- ✅ 逻辑删除支持
- ✅ 跨域访问支持
- ✅ 参数校验

## 扩展开发

### 添加新的模块

1. 在 `entity/` 目录创建实体类
2. 在 `mapper/` 目录创建 Mapper 接口
3. 在 `service/` 目录创建服务接口和实现类
4. 在 `controller/` 目录创建控制器

### 添加拦截器

在 `config/` 目录创建 WebMvcConfigurer 配置类

### 添加定时任务

使用 `@Scheduled` 注解配合 `@EnableScheduling`

## 注意事项

1. 生产环境请修改 JWT Secret
2. 建议创建 `application-local.yml` 用于本地开发
3. 及时修改默认管理员密码
