package com.ctrip.ibu.flow.monitor.service.qmq;

import com.ctrip.framework.foundation.Foundation;
import com.ctrip.ibu.flow.monitor.service.annotations.RedisCluster;
import com.ctrip.ibu.flow.monitor.service.bo.*;
import com.ctrip.ibu.flow.monitor.service.constant.DefaultSplits;
import com.ctrip.ibu.flow.monitor.service.constant.TimeConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import qunar.tc.qmq.Message;
import qunar.tc.qmq.consumer.MessageConsumerProvider;
import com.ctrip.ibu.flow.monitor.service.rule.Rule;
import com.ctrip.ibu.flow.monitor.service.service.FlowHandleService;
import com.ctrip.ibu.flow.monitor.service.tool.json.JsonUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 处理流量上报
 *
 * @author Ian
 * @date 2022/6/3
 */
@Slf4j
@Component
public class DefaultFlowInfoConsumer implements FlowInfoConsumer {

    private final MessageConsumerProvider messageConsumer = new MessageConsumerProvider();

    @Autowired
    private FlowHandleService flowHandleService;

    private final static String REFRESH_TYPE = "refresh";
    private final static String SAMPLE_TYPE = "sample";
    private final static String SUBJECT = "ibu.market.flow.collect";

    public DefaultFlowInfoConsumer() {
        init();
    }

    public void init() {
        messageConsumer.init();
        messageConsumer.addListener(SUBJECT, String.format("%s-flow-monitor-server",
                Foundation.app().getAppId()), this::handle);
    }

    public void handle(Message msg) {
        List<FlowReportInfo> flowReportInfo = buildReport(msg);
        if (!Objects.isNull(flowReportInfo)) {
            for (FlowReportInfo reportInfo : flowReportInfo) {
                doHandle(reportInfo);
            }
        }
    }

    public void doHandle(FlowReportInfo flowReportInfo) {
        if (Objects.isNull(flowReportInfo) || Objects.isNull(flowReportInfo.getSubjectInfo())) {
            return;
        }
        // 设置默认分片
        if (Objects.isNull(flowReportInfo.getSubjectInfo().getSplits())) {
            SubjectInfo subjectInfo = flowReportInfo.getSubjectInfo();
            subjectInfo.setSplits(getDefaultSplits(subjectInfo.getTimeUnit()));
        }
        String type = flowReportInfo.getType();
        if (REFRESH_TYPE.equals(type)) {
            FlowRefresh flowRefresh = buildFlowRefreshInfo(flowReportInfo);
            flowHandleService.doRefresh(flowRefresh);
        } else if (SAMPLE_TYPE.equals(type)) {
            FlowSample flowSample = buildFlowSampleInfo(flowReportInfo);
            flowHandleService.doSample(flowSample);
        }
    }


    private List<FlowReportInfo> buildReport(Message msg) {
        String data = msg.getStringProperty("data");
        if (!Objects.isNull(data)) {
            return JsonUtils.readValues(data, FlowReportInfo.class);
        }
        return null;
    }

    private FlowRefresh buildFlowRefreshInfo(FlowReportInfo reportInfo) {
        SubjectInfo subjectInfo = reportInfo.getSubjectInfo();
        // 时间转换
        subjectInfo.setSpan(convertTime(subjectInfo.getSpan(), subjectInfo.getTimeUnit()));
        return FlowRefresh.builder()
                .subjectInfo(subjectInfo)
                .key(reportInfo.getKey())
                .count(reportInfo.getCount())
                .rules(convertRules(reportInfo.getRules()))
                .timestamp(reportInfo.getTimestamp()).build();
    }

    private List<Rule> convertRules(List<RuleReportInfo> infos) {
        return infos.stream().map(RuleReportInfo::convert).collect(Collectors.toList());
    }

    private FlowSample buildFlowSampleInfo(FlowReportInfo reportInfo) {
        SubjectInfo subjectInfo = reportInfo.getSubjectInfo();
        // 时间转换
        subjectInfo.setSpan(convertTime(subjectInfo.getSpan(), subjectInfo.getTimeUnit()));
        List<RuleReportInfo> ruleReportInfos = reportInfo.getRules();
        if (Objects.isNull(ruleReportInfos) || ruleReportInfos.isEmpty()) {
            return null;
        }
        return FlowSample.builder()
                .subjectInfo(subjectInfo)
                .interval(ruleReportInfos.get(0).getInterval())
                .rule(convertRules(ruleReportInfos).get(0)).build();
    }

    private int getDefaultSplits(String timeUnit) {
        return DefaultSplits.get(timeUnit);
    }

    private long convertTime(long span, String timeUnit) {
        return span * TimeConvert.convert(timeUnit);
    }
}
