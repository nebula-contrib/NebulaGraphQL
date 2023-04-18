<p align="center">
  <br> English | <a href="README-CN.md">中文</a>
  <br>GraphQL Java Library for NebulaGraph<br>
</p>

# NebulaGraphQL

## Introduction

NebulaGraphQL aims to support GraphQL query for NebulaGraph Database.

## Install

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

## Usage

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

## How to contribute

# quick-start

```
docker-compose -f docker-compose.dev.yml up --build
```
