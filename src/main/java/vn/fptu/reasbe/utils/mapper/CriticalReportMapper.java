package vn.fptu.reasbe.utils.mapper;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.stereotype.Component;
import vn.fptu.reasbe.model.dto.criticalreport.CriticalReportResidentRequest;
import vn.fptu.reasbe.model.dto.criticalreport.CriticalReportResponse;
import vn.fptu.reasbe.model.dto.criticalreport.CriticalReportStaffRequest;
import vn.fptu.reasbe.model.entity.CriticalReport;

@Mapper(
        collectionMappingStrategy = CollectionMappingStrategy.SETTER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        uses = {
                UserMapper.class,
                FeedbackMapper.class,
                ExchangeRequestMapper.class
        }
)
@Component
public interface CriticalReportMapper {

    CriticalReport toCriticalReport(CriticalReportResidentRequest report);

    @Mapping(target = "id", ignore = true)
    void updateCriticalReport(@MappingTarget CriticalReport report, CriticalReportStaffRequest reportStaff);

    CriticalReportResponse toCriticalReportResponse(CriticalReport report);

}
