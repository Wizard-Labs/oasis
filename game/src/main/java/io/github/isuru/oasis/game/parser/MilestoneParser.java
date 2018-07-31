package io.github.isuru.oasis.game.parser;

import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.AggregatorType;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.MilestonesDef;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class MilestoneParser {

    public static List<Milestone> parse(List<MilestoneDef> milestoneDefs) {
        List<Milestone> milestones = new LinkedList<>();
        for (MilestoneDef milestoneDef : milestoneDefs) {
            Milestone milestone = new Milestone();
            milestone.setId(milestoneDef.getId());
            milestone.setName(milestoneDef.getName());
            milestone.setDisplayName(milestoneDef.getDisplayName());
            milestone.setEvent(milestoneDef.getEvent());
            milestone.setFrom(milestoneDef.getFrom());
            if (Utils.isNonEmpty(milestoneDef.getPointIds())) {
                milestone.setPointIds(new HashSet<>(milestoneDef.getPointIds()));
            }

            String type = milestoneDef.getAggregator() != null
                    ? milestoneDef.getAggregator()
                    : AggregatorType.COUNT.name();

            milestone.setAggregator(AggregatorType.valueOf(type.toUpperCase()));
            if (milestoneDef.getAccumulator() != null) {
                milestone.setAccumulatorExpr(Utils.compileExpression(milestoneDef.getAccumulator()));
            }

            if (milestoneDef.getCondition() != null) {
                milestone.setCondition(Utils.compileExpression(milestoneDef.getCondition()));
            }

            List<Milestone.Level> levels = new LinkedList<>();
            int doubleCount = 0;
            boolean asRealValues = "double".equalsIgnoreCase(milestoneDef.getAccumulatorType());
            for (Map.Entry<Integer, Object> entry : milestoneDef.getLevels().entrySet()) {
                Milestone.Level level = new Milestone.Level();
                level.setLevel(entry.getKey());
                Number number = interpretNumber(entry.getValue(), asRealValues);
                level.setNumber(number);
                level.setAwardPoints(milestoneDef.getAwardPoints(entry.getKey()));

                if (Double.class.isAssignableFrom(number.getClass())) {
                    doubleCount++;
                }
                levels.add(level);
            }
            milestone.setLevels(levels);

            if (milestoneDef.getAccumulatorType() == null) {
                milestone.setRealValues(doubleCount > 0);
            } else {
                milestone.setRealValues(asRealValues);
            }

            milestones.add(milestone);
        }
        return milestones;
    }

    public static List<Milestone> parse(InputStream inputStream) {
        Yaml yaml = new Yaml();
        MilestonesDef milestonesDef = yaml.loadAs(inputStream, MilestonesDef.class);

        return parse(milestonesDef.getMilestones());
    }

    private static Number interpretNumber(Object val, boolean asRealValues) {
        if (Integer.class.isAssignableFrom(val.getClass()) || Long.class.isAssignableFrom(val.getClass())) {
            return asRealValues ? ((Number)val).doubleValue() : ((Number)val).longValue();
        } else if (Double.class.isAssignableFrom(val.getClass())) {
            return ((Number)val).doubleValue();
        } else {
            Double d = Utils.strNum(val.toString());
            return asRealValues ? d : d.longValue();
        }
    }

}