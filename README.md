# Hyena


[![Travis (.org) branch](https://img.shields.io/travis/alphajiang/hyena/master.svg)](https://travis-ci.org/alphajiang/hyena)
[![Coverage Status](https://coveralls.io/repos/github/alphajiang/hyena/badge.svg?branch=master)](https://coveralls.io/github/alphajiang/hyena?branch=master)
[![Maven](https://img.shields.io/maven-central/v/io.github.alphajiang/hyena-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.alphajiang)
[![License](https://img.shields.io/github/license/alphajiang/hyena.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

Universal Block Token (通配权) 权证服务
## 通配权积分相关接口
更多接口介绍可查看swagger文档. swagger-ui的访问URL /swagger-ui.html
 
### 增加通配权
+ 给指定用户增加通配权,调用成功后返回该用户的通配权明细.
+ URL: /ubt/point/increase
+ Http Method: Post
+ Content-Type: application/json;charset=utf-8
+ 请求参数

| 参数名 | 类型 | 含义 | 必传 | 备注 |
| :-- | :-- | :-- | :-- | :-- |
| seq | string | 请求序列号. <br />1, 用于匹配请求消息和响应消息; <br />2, 做接口幂等性校验. <br />序列号为空时表示不做匹配及幂等性校验. |  | 每次新的请求使用不同的随机字串. 如果是重送请求使用相同的序列号 |
| type | string | 通配权类型 | | 可自定义类型, 用于存储'积分', '余额', 'XX币'等. 为空时, 自动创建类型为'default'的通配权 |
| uid | string | 用户标识 | 是 | |
| point | number | 通配权数量 | 是 | 要增加的通配权数量 |
| expireTime | string | 过期时间. 不传表示永不过期. | | 格式为 "yyyy-MM-dd HH:mm:ss". <br />如: 2018-10-25 18:34:32 表示2018年10月25日18点34分32秒过期 |
| tag | string | 自定义标签 |  | |
| note | string | 备注 | ||
 
+ 请求消息示例
```
{
    "uid" : "user_123",
    "point" : 987654
}    
```

+ 返回结果

| 参数名 | 类型 | 含义 | 备注 |
| :-- | :-- | :-- | :-- | 
| status | number | 接口调用返回结果 | 0 表示成功, 其他都表示接口调用失败 |
| seq | string | 请求序列号 | 返回请求消息里的序列号. <br />仅当请求消息有seq时, 响应消息才会返回seq. |
| data.uid | string | 用户标识 | |
| data.point | number | 用户总有效通配权 | |
| data.available | number | 当前可用通配权 | |
| data.used | number | 已使用的通配权 | |
| data.frozen | number | 当前冻结的通配权 | |
| data.expire | number | 已过期的通配权 | |

 + 返回结果示例
```
{
    "status" : 0,
    "data" : {
        "uid" : "user_123",
        "point" : 987654,
        "available" : 987654,
        "used" : 0,
        "frozen" : 0,
        "expire" : 0
    }
}    
```

### 冻结通配权
+ 冻结指定用户的通配权,被冻结通配权的数量不能超过当前用户的可用通配权. 调用成功后返回用户通配权明细.
+ URL: /ubt/point/freeze
+ Http Method: Post
+ Content-Type: application/json;charset=utf-8
+ 请求参数

| 参数名 | 类型 | 含义 | 必传 | 备注 |
| :-- | :-- | :-- | :-- | :-- |
| seq | string | 请求序列号.  |  |  |
| type | string | 通配权类型 | | 与增加通配权时的类型一致 |
| uid | string | 用户标识 | 是 | |
| point | number | 通配权数量 | 是 | 要冻结的通配权数量 |
| note | string | 备注 | ||

+ 返回结果. 数据结构与增加通配权的数据结构一致.

### 解冻通配权
+ 解冻指定用户的通配权,解冻的通配权数量不能超过当前用户已冻结的通配权数量. 调用成功后返回用户通配权明细.
+ URL: /ubt/point/unfreeze
+ Http Method: Post
+ Content-Type: application/json;charset=utf-8
+ 请求参数

| 参数名 | 类型 | 含义 | 必传 | 备注 |
| :-- | :-- | :-- | :-- | :-- |
| seq | string | 请求序列号.  |  |  |
| type | string | 通配权类型 | | 与冻结通配权时的类型一致 |
| uid | string | 用户标识 | 是 | |
| point | number | 通配权数量 | 是 | 要解冻的通配权数量 |
| note | string | 备注 | ||

+ 返回结果. 数据结构与增加通配权的数据结构一致.

### 消费通配权
+ 用户通配权消费后调用该接口. 被消费的通配权数量不能超过当前可用通配权. 调用成功后返回用户通配权明细.
+ URL: /ubt/point/decrease
+ Http Method: Post
+ Content-Type: application/json;charset=utf-8
+ 请求参数

| 参数名 | 类型 | 含义 | 必传 | 备注 |
| :-- | :-- | :-- | :-- | :-- |
| seq | string | 请求序列号.  |  |  |
| type | string | 通配权类型 | | 与增加通配权时的类型一致 |
| uid | string | 用户标识 | 是 | |
| point | number | 通配权数量 | 是 | 要消费的通配权数量 |
| note | string | 备注 | ||

+ 返回结果. 数据结构与增加通配权的数据结构一致.

### 消费已冻结通配权
+ 用户消费已冻结的通配权后调用该接口. 被消费的通配权数量不能超过当前已冻结的通配权. 调用成功后返回用户通配权明细.
+ URL: /ubt/point/decreaseFrozen
+ Http Method: Post
+ Content-Type: application/json;charset=utf-8
+ 请求参数

| 参数名 | 类型 | 含义 | 必传 | 备注 |
| :-- | :-- | :-- | :-- | :-- |
| seq | string | 请求序列号.  |  |  |
| type | string | 通配权类型 | | 与冻结通配权时的类型一致 |
| uid | string | 用户标识 | 是 | |
| point | number | 通配权数量 | 是 | 要消费的通配权数量 |
| note | string | 备注 | ||

+ 返回结果. 数据结构与增加通配权的数据结构一致.

### 撤销通配权
+ 撤销之前给用户增加的通配权. 调用成功后返回用户通配权明细.
+ URL: /ubt/point/cancel
+ Http Method: Post
+ Content-Type: application/json;charset=utf-8
### 获取用户通配权列表
+ 按条件查询用户列表.
+ URL: /ubt/point/listPoint
+ Http Method: GET
+ Content-Type: application/json;charset=utf-8
### 获取通配权明细列表
+ 按条件查询用户通配权记录.
+ URL: /ubt/point/listPointRecord
+ Http Method: GET
+ Content-Type: application/json;charset=utf-8



## 示例代码
Maven
```
<dependency>
    <groupId>io.github.alphajiang</groupId>
    <artifactId>hyena-spring-boot-starter</artifactId>
    <version>0.0.5</version>
</dependency>
```
Gradle
```
plugins {
	id 'org.springframework.boot' version '2.1.6.RELEASE'
	id 'java'
}
apply plugin: 'io.spring.dependency-management'
dependencies {
    implementation("io.github.alphajiang:hyena-spring-boot-starter:0.0.5")
	implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.1.0'
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")

	runtimeOnly 'mysql:mysql-connector-java'
}
```
Java代码
```
@SpringBootApplication
@ComponentScan({ "io.github.alphajiang.hyena" })
@MapperScan(basePackages = { "io.github.alphajiang.hyena.ds.mapper" })
@EnableTransactionManagement
@EnableScheduling
public class HyenaMain {
    public static void main(String[] args) {
        new SpringApplicationBuilder(HyenaMain.class).web(WebApplicationType.SERVLET).run(args);
    }
}
```


  

