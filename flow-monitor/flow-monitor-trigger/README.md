### 一、monitor-trigger自定义流量订阅响应事件
#### 1. 基于API实现
1. 调用 Trigger.watch()方法
 
	```java
	/**  
	 * 事件注册  
	 * @param key appId-subjectName-ruleName  
	 * @param event 自定义响应事件  
	 */  
	public static void watch(String key, Event event);
	```
	调用示例：
	```java
	@Service  
	public class FlowEventTest implements Event {  
	     
		 @PostConstruct  
		 private void init() {  
		       Trigger.watch("subjectTest-rule1", this);  
		 }  
		 
		 @Autowired  
		 private CampaignTaskService campaignTaskService;  
		 
		 @PostConstruct  
		 public void register() {  
		       Trigger.watch("subjectTest-rule1", this);  
		 }  
		 
		 @Override  
		 public void doEvent(List<FlowInfo> flowInfo) {  
		        System.out.println(campaignTaskService);  
				for (FlowInfo info : flowInfo) {  
			         System.out.println("count:" + info.getCount());  
					 System.out.println("increment:" + info.getIncrement());  
					 System.out.println("watch:" + info.getWatch());  
					 System.out.println("key:" + info.getKey());  
					 System.out.println("timestamp:" + info.getTimestamp());  
					 System.out.println("span" + info.getSpan());  
				 }  
		   }  
 	 }
	```
	注意：Trigger.watch()不具有幂等性， 只用执行一次，不用反复注册，尽量把Trigger.watch() 调用放在初始化代码中，而不是被反复调用的业务代码
#### 2. 基于注解实现
1. 实现接口Event，实现doEvent()方法
2. 打上注解@FlowWatchEvent和Spring的单例注解 <br>

	使用示例：

	```java
	@Service  
	// watch（规则订阅）：appId-subjectName-ruleName 或者 subjectName-ruleName，appId缺省时默认本地appId  
	@FlowWatchEvent(watch = {"subjectTest-rule1", "subjectTest-rule2", "subjectTest-rule3"})  
	public class FlowEventTest implements Event {  
		@Override  
		public void doEvent(List<FlowInfo> flowInfos) {
		    FlowInfo flowInfo = flowInfos.get(0);  
			System.out.println("key:" + flowInfo.getKey()); // 触发规则的key  
			System.out.println("count:" + flowInfo.getCount()); // 触发规则时的流量count  
			System.out.println("time:" + flowInfo.getTimestamp()); // 触发规则时的时间（时间戳）  
			System.out.println("span:" + flowInfo.getSpan()); // 流量统计时间窗长度，[timestamp - span, timestamp]为流量的统计区间  
			System.out.println("increment:" + flowInfo.getIncrement()); // 流量增量，实时订阅时有效，定时订阅默认为0  
			System.out.println("watch:" + flowInfo.getWatch()); // 触发规则的watch（appId-subjectName-ruleName）  
		}  
	}
	```
