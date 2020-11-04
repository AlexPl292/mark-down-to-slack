# mark-down-to-slack

Markdown to Slack-markdown converter for java.

## Adding as a dependency

Gradle:

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.AlexPl292:mark-down-to-slack:1.1.2'
}
```

Or visit [jitpack](https://jitpack.io/#AlexPl292/mark-down-to-slack) to find out other ways.

## Usage

```java
import dev.feedforward.markdownto.DownParser

public static void main() {
  String markdown = "#Header";
  String slackMarkdown = new DownParser(input).toSlack().toString();
  System.out.println(slackMarkdown);
}
```
