---
title: 实时交易
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
code_clipboard: true
highlight_theme: darkula
headingLevel: 2
generator: "@tarslib/widdershins v4.0.28"

---

# 实时交易

Base URLs:

# Authentication

# Default

## POST 创建账户

POST /api/v1/accounts

> Body 请求参数

```json
{
  "currency": "CNY"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|object| 否 |none|
|» currency|body|string| 是 |none|

> 返回示例

```json
{
  "accountId": 10000000000,
  "balance": 1000,
  "currency": "CNY",
  "accountType": "DEBIT",
  "created": "2025-02-17T04:02:43.253998"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» accountId|integer|true|none||none|
|» balance|integer|true|none||none|
|» currency|string|true|none||none|
|» accountType|string|true|none||none|
|» created|string|true|none||none|

## POST 处理交易

POST /api/v1/transactions

> Body 请求参数

```json
{
  "currency": "CNY"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|object| 否 |none|
|» bizId|body|string| 是 |none|
|» sourceAccount|body|string| 是 |none|
|» targetAccount|body|string| 是 |none|
|» currency|body|string| 是 |none|
|» amount|body|number| 是 |none|

> 返回示例

```json
{
  "statusCode": 0,
  "message": "success",
  "data": {
    "bizId": "txn_123456AB11",
    "status": "SUCCESS",
    "error": null,
    "created": "2025-02-17 16:18:08"
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» statusCode|integer|true|none||none|
|» message|string|true|none||none|
|» data|object|true|none||none|
|»» bizId|string|true|none||none|
|»» status|string|true|none||none|
|»» error|null|true|none||none|
|»» created|string|true|none||none|

## GET 获取账户信息

GET /api/v1/accounts/{accountId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|accountId|path|string| 是 |none|

> 返回示例

```json
{
  "statusCode": 0,
  "message": "success",
  "data": {
    "accountId": 10000000000,
    "balance": 1000,
    "currency": "CNY",
    "accountType": "DEBIT",
    "created": "2025-02-17 04:02:43"
  }
}
```

```json
{
  "statusCode": 1005,
  "message": "account not found:accountId:100000020000"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» statusCode|integer|true|none||none|
|» message|string|true|none||none|
|» data|object|true|none||none|
|»» accountId|integer|true|none||none|
|»» balance|number|true|none||none|
|»» currency|string|true|none||none|
|»» accountType|string|true|none||none|
|»» created|string|true|none||none|

## GET 获取交易状态

GET /api/v1/transactions/{bizId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|bizId|path|string| 是 |none|

> 返回示例

```json
{
  "statusCode": 0,
  "message": "success",
  "data": {
    "bizId": "txn_123456AB8",
    "error": null,
    "status": "SUCCESS",
    "amount": 210.5,
    "sourceAccount": 10000000001,
    "targetAccount": 10000000004,
    "created": "2025-02-17 14:32:12",
    "updated": "2025-02-17 15:21:54"
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» statusCode|integer|true|none||none|
|» message|string|true|none||none|
|» data|object|true|none||none|
|»» bizId|string|true|none||none|
|»» error|null|true|none||none|
|»» status|string|true|none||none|
|»» amount|number|true|none||none|
|»» sourceAccount|integer|true|none||none|
|»» targetAccount|integer|true|none||none|
|»» created|string|true|none||none|
|»» updated|string|true|none||none|

# 数据模型

