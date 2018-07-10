package io.github.isuru.oasis.parser;

import io.github.isuru.oasis.Event;
import io.github.isuru.oasis.utils.Utils;
import io.github.isuru.oasis.model.rules.PointRule;
import io.github.isuru.oasis.parser.model.PointDef;
import io.github.isuru.oasis.parser.model.PointsAdditional;
import io.github.isuru.oasis.parser.model.PointsDef;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.util.Preconditions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author iweerarathna
 */
public class PointParser {

    public static List<PointRule> parse(InputStream inputStream) throws IOException {
        Yaml yaml = new Yaml();
        PointsDef pointsDef = yaml.loadAs(inputStream, PointsDef.class);

        List<PointRule> pointRules = new LinkedList<>();
        List<PointDef> pointsRef = pointsDef.getPoints();
        for (PointDef record : pointsRef) {
            PointRule pr = new PointRule();
            pr.setId(record.getId());
            pr.setForEvent(record.getEvent());
            pr.setSource(record.getSource());

            Object amount = record.getAmount();
            if (amount != null) {
                if (amount instanceof Number) {
                    pr.setAmount(((Number) amount).intValue());
                } else if (amount instanceof String) {
                    pr.setAmountExpression(amount.toString());
                } else {
                    throw new IllegalArgumentException("'amount' field missing or does not have type neither number nor string!");
                }
            }

            Object conditionClass = record.getConditionClass();
            if (conditionClass != null) {
                FilterFunction<Event> o = Utils.loadInstanceOfClz(conditionClass.toString(),
                        Thread.currentThread().getContextClassLoader());
                pr.setConditionClass(o);
            }

            pr.setCondition(record.getCondition());

            if (record.getAdditionalPoints() != null) {
                List<PointRule.AdditionalPointReward> rewards = new LinkedList<>();
                for (PointsAdditional pa : record.getAdditionalPoints()) {
                    precheck(pa, record);

                    PointRule.AdditionalPointReward apr = new PointRule.AdditionalPointReward();
                    apr.setToUser(pa.getToUser());
                    apr.setId(pa.getId());

                    if (pa.getAmount() instanceof Number) {
                        apr.setAmount((Number) pa.getAmount());
                    } else {
                        apr.setAmount(Utils.compileExpression(pa.getAmount().toString()));
                    }
                    rewards.add(apr);
                }
                pr.setAdditionalPoints(rewards);
            }

            pointRules.add(pr);
        }
        return pointRules;
    }

    private static void precheck(PointsAdditional pa, PointDef rule) {
        Preconditions.checkArgument(pa.getId() != null,
                "missing field 'id' for one of additionalPoint entry in rule '" + rule.getId() + "'!");
        Preconditions.checkArgument(pa.getToUser() != null,
                "missing field 'toUser' for one of additionalPoint entry in rule '" + rule.getId() + "'!");
    }
}
