package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.common.BusinessException;
import com.campuslink.common.ResultCode;
import com.campuslink.entity.Evaluation;
import com.campuslink.entity.EvaluationReply;
import com.campuslink.entity.Report;
import com.campuslink.entity.User;
import com.campuslink.mapper.EvaluationMapper;
import com.campuslink.mapper.EvaluationReplyMapper;
import com.campuslink.mapper.ReportMapper;
import com.campuslink.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价业务，v2 新增。
 *
 * <p>规则：
 * 1. 提交评价：写 evaluation + 更新被评价人 reputation（平均分换算 5 分制）
 * 2. 回复评价 / 举报评价
 * 3. 信誉分明细 / 排行榜
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationService.class);

    private final EvaluationMapper evaluationMapper;
    private final EvaluationReplyMapper replyMapper;
    private final ReportMapper reportMapper;
    private final UserMapper userMapper;

    public Evaluation submit(Long teamId, Long fromUserId, Long toUserId,
                             int responsibility, int tech, int communication) {
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException("不能给自己评价");
        }

        Long exists = evaluationMapper.selectCount(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getTeamId, teamId)
                .eq(Evaluation::getFromUserId, fromUserId)
                .eq(Evaluation::getToUserId, toUserId));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.DUPLICATE_RECORD, "已评价过该成员");
        }

        Evaluation eval = new Evaluation();
        eval.setTeamId(teamId);
        eval.setFromUserId(fromUserId);
        eval.setToUserId(toUserId);
        eval.setResponsibility(responsibility);
        eval.setTech(tech);
        eval.setCommunication(communication);
        evaluationMapper.insert(eval);
        LOGGER.info("评价已提交, evalId={}, toUserId={}", eval.getId(), toUserId);

        recalcReputation(toUserId);
        return eval;
    }

    public List<Evaluation> listByUser(Long userId) {
        return evaluationMapper.selectList(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getToUserId, userId)
                .orderByDesc(Evaluation::getCreateTime));
    }

    public EvaluationReply reply(Long evaluationId, String content, Long authorId) {
        Evaluation eval = evaluationMapper.selectById(evaluationId);
        if (eval == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        EvaluationReply reply = new EvaluationReply();
        reply.setEvaluationId(evaluationId);
        reply.setAuthorId(authorId);
        reply.setContent(content);
        replyMapper.insert(reply);
        LOGGER.info("评价回复已提交, evalId={}, authorId={}", evaluationId, authorId);
        return reply;
    }

    public List<EvaluationReply> listReplies(Long evaluationId) {
        return replyMapper.selectList(new LambdaQueryWrapper<EvaluationReply>()
                .eq(EvaluationReply::getEvaluationId, evaluationId)
                .orderByAsc(EvaluationReply::getCreateTime));
    }

    public Report report(Long evaluationId, String reason, Long reporterId) {
        if (evaluationMapper.selectById(evaluationId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        Report report = new Report();
        report.setTargetType("EVALUATION");
        report.setTargetId(evaluationId);
        report.setReporterId(reporterId);
        report.setReason(reason);
        report.setStatus("PENDING");
        reportMapper.insert(report);
        LOGGER.info("评价已举报, reportId={}, evalId={}", report.getId(), evaluationId);
        return report;
    }

    /** 信誉分明细 */
    public Map<String, Object> reputationDetail(Long userId) {
        List<Evaluation> list = listByUser(userId);
        if (list.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("total", 0);
            empty.put("avgResponsibility", 0);
            empty.put("avgTech", 0);
            empty.put("avgCommunication", 0);
            empty.put("items", List.of());
            return empty;
        }

        double respSum = list.stream().mapToInt(Evaluation::getResponsibility).sum();
        double techSum = list.stream().mapToInt(Evaluation::getTech).sum();
        double commSum = list.stream().mapToInt(Evaluation::getCommunication).sum();
        Map<String, Object> data = new HashMap<>();
        data.put("total", list.size());
        data.put("avgResponsibility", round(respSum / list.size()));
        data.put("avgTech", round(techSum / list.size()));
        data.put("avgCommunication", round(commSum / list.size()));
        data.put("items", list);
        return data;
    }

    /** 信誉分排行榜 */
    public List<Map<String, Object>> ranking(int limit) {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "STUDENT")
                .orderByDesc(User::getReputation));
        List<Map<String, Object>> result = new ArrayList<>();
        int top = Math.min(limit <= 0 ? 50 : limit, users.size());
        for (int i = 0; i < top; i++) {
            User u = users.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("rank", i + 1);
            item.put("userId", u.getId());
            item.put("nickname", u.getNickname());
            item.put("reputation", u.getReputation());
            item.put("college", u.getCollege());
            result.add(item);
        }
        result.sort(Comparator.comparingInt(o -> ((Number) o.get("rank")).intValue()));
        return result;
    }

    private void recalcReputation(Long userId) {
        List<Evaluation> list = listByUser(userId);
        if (list.isEmpty()) {
            return;
        }
        double avg = list.stream()
                .mapToDouble(e -> (e.getResponsibility() + e.getTech() + e.getCommunication()) / 3.0)
                .average()
                .orElse(5.0);
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setReputation(round(avg));
            userMapper.updateById(user);
            LOGGER.info("信誉分已重算, userId={}, reputation={}", userId, user.getReputation());
        }
    }

    private BigDecimal round(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }
}
