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

Publish messages from your plugin
------------------------

This plugin provides convenient interfaces to publish messages to RabbitMQ.

If you want to use them from your plugin, please see the implementation of [RabbitMQ Build Trigger Plugin][rabbitmq-build-trigger].

Implementaion class:

> org.jenkinsci.plugins.rabbitmqbuildtrigger.RemoteBuildPublisher

Interfaces:

> org.jenkinsci.plugins.rabbitmqconsumer.publishers.PublishChannelFactory
> org.jenkinsci.plugins.rabbitmqconsumer.publishers.PublishChannel
> org.jenkinsci.plugins.rabbitmqconsumer.publishers.PublishResult
> org.jenkinsci.plugins.rabbitmqconsumer.publishers.ExchangeType
> org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQChannelListener

Notice
------------------------

This plugin does not generates any queues in RabbitMQ. So you should prepare them by yourself.

Material
------------------------

* [RabbitMQ Build Trigger Plugin][rabbitmq-build-trigger]

[rabbitmq-build-trigger]: http://wiki.jenkins-ci.org/display/JENKINS/RabbitMQ+Build+Trigger+Plugin
[source-rabbitmq-build-trigger]: https://github.com/jenkinsci/rabbitmq-build-trigger-plugin

License
------------------------

MIT License

Copyright
------------------------

Copyright (c) 2013 rinrinne a.k.a. rin_ne
