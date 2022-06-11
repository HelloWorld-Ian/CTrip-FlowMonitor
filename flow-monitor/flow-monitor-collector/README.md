### 一、使用monitor-collector进行流量收集
#### 1. 基于API
1. 初始化流量收集类FlowCollector
	```java
	public static FlowCollector initCollector() {  
		FlowCollector collector = new DefaultFlowCollector();  
		SubjectInfo subjectInfo = new SubjectInfo();  
		subjectInfo.setAppId(Foundation.app().getAppId()); // 设置appId  
		subjectInfo.setName("subjectTest"); // 设置subject name  
		subjectInfo.setSpan(60L); // 设置统计时间窗长度  
		subjectInfo.setTimeUnit("SECOND"); // 设置时间单位  
		subjectInfo.setSplits(5); // 设置时间窗分片  
		collector.setSubject(subjectInfo);  
		InstantRuleInfo instantRuleInfo = new InstantRuleInfo();  
		instantRuleInfo.setName("rule1"); // rule name  
		instantRuleInfo.setStrategy("MORE_THAN"); // rule strategy  
		instantRuleInfo.setThreshold(100L); // 命中阈值  
		collector.addInstantRule(instantRuleInfo);  

		ScheduledRuleInfo scheduledRuleInfo = new ScheduledRuleInfo();  
		scheduledRuleInfo.setName("rule2");  
		scheduledRuleInfo.setStrategy("LESS_THAN");  
		scheduledRuleInfo.setThreshold(50L);  
		scheduledRuleInfo.setInterval(1000L); // 定时订阅时间间隔  
		scheduledRuleInfo.setTimeUnit("MINUTE"); // 时间间隔单位  
		collector.addScheduleRule(scheduledRuleInfo);  
		if (!collector.isRegistered()) {  
		      collector.register();   // 注册collector
		}  
		return collector;
	}
	```
2. 执行方法collect()
	```java
	public static void main(String[] args) {  
	    FlowCollector collector = initCollector();  
	    collector.collect("keyTest");  
	}
	```

#### 2. 基于QConfig
1. 定义QConfig文件flow-monitor-subject.json
	```json
	[  
		{  
			"name": "test", 
			"span": 60,   
			"splits": 5,  
			"timeUnit": "SECOND",   
			"instantSubscribe": [       
				{  
				"name": "rule1",  
				"strategy": "MORE_THAN",  
				"threshold": 5   
				}  
			],  
			"scheduleSubscribe": [  
				{  
				"name": "rule2",   
				"strategy": "MORE_THAN",  
				"threshold": 10,  
				"interval": 30,  
				"timeUnit": "SECOND"   
				}  
			]  
		}  
	]
	```

2. 执行Collector.doCollect( )方法
	 ```java
	 // doCollect(String subjectName, String key)
	FlowCollectorFactory.doCollect("subjectTest", "testKey");
	 ```

#### 3. 基于注解
1. 定义QConfig文件flow-monitor-subject.json
	```json
	[  
		{  
			"name": "test", 
			"span": 60,   
			"splits": 5,  
			"timeUnit": "SECOND",   
			"instantSubscribe": [       
				{  
				"name": "rule1",  
				"strategy": "MORE_THAN",  
				"threshold": 5   
				}  
			],  
			"scheduleSubscribe": [  
				{  
				"name": "rule2",   
				"strategy": "MORE_THAN",  
				"threshold": 10,  
				"interval": 30,  
				"timeUnit": "SECOND"   
				}  
			]  
		}  
	]
	```
2. 在需要进行流量收集的方法上添加注解
	```java
		// subject: subjectName
		// key: 流量统计的key值，${}进行方法参数映射，映射格式为：${参数位置索引: field path} or ${参数位置索引}
		@FlowMonitor(subject = "subjectTest", key = "test-${0:info/0/name}-${0/id}-${1}")  
		public void flowMonitorTest(TestRequest request, String ip) {  
		    // do you business  
		}  
		  
		@Getter  
		@Builder  
		private static class TestRequest {  
		    List<Info> info;  
		    String id;  
		}  
	  
		@Getter  
		@Builder  
		private static class Info {  
		    String name;  
		}
	```

