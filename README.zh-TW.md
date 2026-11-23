[English](README.md) | [簡體中文](README.zh-CN.md) | [繁體中文](README.zh-TW.md)

# 訊息資源 Spring Boot Starter

讓 Spring Boot 應用快速整合 `MessageSource`，自動偵測資源包屬性檔案。

## 快速開始

- 匯入相依性

```xml
    <dependency>
        <groupId>com.jiovan.spring.starter</groupId>
        <artifactId>message-resource-spring-boot-starter</artifactId>
        <version>LATEST</version>
    </dependency>
```

> 預設情況下，此 starter 會自動生效，你可以透過 `jiovan.message-resource.enabled = false` 關閉它。

- 使用字首 `jiovan.message-resource` 設定 Spring Boot 的 `application.yml`（**可選**）

```yml
jiovan:
    message-resource:
        default-locale: en-US
        message-bundle:
            scan-classpath: true
            scan-recursive: true
            scan-resources:
                - 'lang'
```
這將建立一個 `MessageSource` Bean，自動掃描 **classpath** 下 `scan-resources` 路徑中的檔案。

- 可選功能：與 Spring 的 `MessageSourceAware` 介面類似，此 starter 宣告瞭一個帶有 `MessageSourceAccessorAware` 的處理器，你可以讓 Bean 實現該介面，Spring 將注入一個 `MessageSourceAccessor` 例項，隨後即可解析訊息。這是使用 `MessageSource` 的另一種方式。

## 文件

- GitHub：https://github.com/jiovan-labs/message-resource-spring-boot-starter

## 環境要求

- jdk 17+

## 許可證

本專案採用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 許可證。

請參閱 `NOTICE.txt` 檔案瞭解所需的宣告與歸屬資訊。

## 捐贈

喜歡這個工具包嗎？歡迎[向我們捐贈](https://jiovan.com/donation)以支援開發。

## 版權

北京玖漫科技有限公司

## 網站

- 玖漫：https://jiovan.com
