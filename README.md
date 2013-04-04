rabbitmq-consumer: RabbitMQ Consumer Plugin for Jenkins
=======================================================

* Author: rinrinne a.k.a. rin_ne
* Repository: http://github.com/jenkinsci/rabbitmq-consumer-plugin
* Plugin Information: https://wiki.jenkins-ci.org/display/JENKINS/RabbitMQ+Consumer+Plugin

Synopsis
------------------------

rabbitmq-consumer is a Jenkins plugin to connect to RabbitMQ then consume application messages in specific queues.

This plugin has global configuration only, so any features for user are not provided. You would need other plugins to provide features for user.

Develop listener plugin
------------------------

This plugin provides convenient interfaces to listen application messages.

To implement listener to your plugin, the below setups are needed.

in pom.xml:

```xml
<project>

  <dependencies>
    <dependency>
      <groupId>org.jenkins-ci.plugins</groupId>
      <artifactId>rabbitmq-consumer</artifactId>
      <version>VERSION</version>
    </dependency>
  </dependencies>

</project>
```

Interface:

> org.jenkinsci.plugins.rabbitmqconsumer.listeners.ApplicationMessageListener

Notice
------------------------

This plugin does not generates any queues in RabbitMQ. So you should prepare them by yourself.

License
------------------------

MIT License

Copyright
------------------------

Copyright (c) 2013 rinrinne a.k.a. rin_ne
