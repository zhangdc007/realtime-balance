# transaction 接口

### POST 新增交易

POST /transactions/

> Body 请求参数

```json
{
  "id": 0,
  "bizId": "string",
  "accountId": "string",
  "type": 0,
  "amount": 0,
  "description": "string",
  "dateTime": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[Transaction](#schematransaction)| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## PUT 修改交易

PUT /transactions/{id}

> Body 请求参数

```json
{
  "id": 0,
  "bizId": "string",
  "accountId": "string",
  "type": 0,
  "amount": 0,
  "description": "string",
  "dateTime": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|string| 是 |none|
|body|body|[Transaction](#schematransaction)| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET 查询交易

GET /transactions/{id}

> Body 请求参数

```json
{
  "id": 0,
  "bizId": "string",
  "accountId": "string",
  "type": 0,
  "amount": 0,
  "description": "string",
  "dateTime": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|string| 是 |none|
|body|body|[Transaction](#schematransaction)| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## DELETE 删除交易

DELETE /transactions/{id}

> Body 请求参数

```json
{
  "id": 0,
  "bizId": "string",
  "accountId": "string",
  "type": 0,
  "amount": 0,
  "description": "string",
  "dateTime": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|string| 是 |none|
|body|body|[Transaction](#schematransaction)| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET 分页查询交易

GET /transactions/page

> Body 请求参数

```json
{
  "id": 0,
  "bizId": "string",
  "accountId": "string",
  "type": 0,
  "amount": 0,
  "description": "string",
  "dateTime": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[Transaction](#schematransaction)| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET 查询全部交易

GET /transactions/all

> Body 请求参数

```json
{
  "id": 0,
  "bizId": "string",
  "accountId": "string",
  "type": 0,
  "amount": 0,
  "description": "string",
  "dateTime": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[Transaction](#schematransaction)| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

# 数据模型

<h2 id="tocS_Transaction">Transaction</h2>

<a id="schematransaction"></a>
<a id="schema_Transaction"></a>
<a id="tocStransaction"></a>
<a id="tocstransaction"></a>

```json
{
  "id": 0,
  "bizId": "string",
  "accountId": "string",
  "type": 0,
  "amount": 0,
  "description": "string",
  "dateTime": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer|true|none|数据库ID|none|
|bizId|string|true|none|业务id|none|
|accountId|string|true|none|账户id|none|
|type|integer|true|none|交易类型|none|
|amount|integer|true|none|交易金额|none|
|description|string|true|none|交易描述|none|
|dateTime|string|true|none|交易时间|none|
