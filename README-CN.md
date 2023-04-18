<p align="center">
  <br>  <a href="README-CN.md">English</a> | 中文
  <br>GraphQL Java Library for NebulaGraph<br>
</p>

# NebulaGraphQL

## 介绍

NebulaGraphQL目标是为NebulaGraph支持GraphQL查询

## 安装

```
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/Dragonchu/NebulaGraphQL</url>
    </repository>
</repositories>
```

```
<dependency>
  <groupId>com.dragonchu</groupId>
  <artifactId>nebula-graphql</artifactId>
  <version>0.0.1</version>
</dependency>
```

## 使用

```
HostAddress metadAddress = new HostAddress("metad0",9559);
HostAddress graphdAddress = new HostAddress("graphd", 9669);
String spaceName = "basketballplayer";
String username = "root";
String password = "nebula";
GraphqlSessionPoolConfig graphqlSessionPoolConfig = new GraphqlSessionPoolConfig(
    Lists.newArrayList(graphdAddress), 
    Lists.newArrayList(metadAddress), 
    spaceName, username, password);
graphqlSessionPoolConfig.setTimeout(3000);
GraphqlSessionPool pool = new GraphqlSessionPool(graphqlSessionPoolConfig);
ExecutionResult executionResult = pool.execute("{players(age:32){name\nage}}");
```

## 参与贡献

# quick-start

```
docker-compose -f docker-compose.dev.yml up --build
```
