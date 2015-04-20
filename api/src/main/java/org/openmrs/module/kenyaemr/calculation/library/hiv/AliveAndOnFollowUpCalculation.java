package org.openmrs.module.kenyaemr.calculation.library.hiv;

import org.openmrs.Program;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyacore.calculation.BooleanResult;
import org.openmrs.module.kenyacore.calculation.CalculationUtils;
import org.openmrs.module.kenyacore.calculation.Filters;
import org.openmrs.module.kenyaemr.calculation.library.MissedLastAppointmentCalculation;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Calculate only patients who are active, exclude lost to follow up patients and those who defaulted care
 */
public class AliveAndOnFollowUpCalculation extends AbstractPatientCalculation {

	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues, PatientCalculationContext context) {

		Program hivProgram = MetadataUtils.existing(Program.class, HivMetadata._Program.HIV);

		Set<Integer> alive = Filters.alive(cohort, context);
		Set<Integer> inHivProgram = Filters.inProgram(hivProgram, alive, context);

		CalculationResultMap ltfu = calculate(new LostToFollowUpCalculation(), cohort, context);
        CalculationResultMap defaulted = calculate(new MissedLastAppointmentCalculation(), cohort, context);
		CalculationResultMap ret = new CalculationResultMap();
		for(Integer ptId: cohort){
			boolean aliveAndOnFollowUp = false;

            Boolean ltfuBoolean = (Boolean) ltfu.get(ptId).getValue();
            Boolean defaultedBoolean = (Boolean) defaulted.get(ptId).getValue();
			 if(inHivProgram.contains(ptId)) {
				aliveAndOnFollowUp = true;
			 }
			if((ltfuBoolean != null && ltfuBoolean.equals(Boolean.TRUE)) || (defaultedBoolean != null && defaultedBoolean.equals(Boolean.TRUE))) {
				aliveAndOnFollowUp = false;
			}
			ret.put(ptId, new BooleanResult(aliveAndOnFollowUp, this));
		}
		return ret;
	}
}
