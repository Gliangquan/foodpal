package com.jcen.medpal.model.dto.food;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(20)
public class ComplaintRankingExportVO {
    
    @ExcelProperty("排名")
    private Integer rank;
    
    @ExcelProperty("商户ID")
    private Long merchantId;
    
    @ExcelProperty("商户名称")
    private String merchantName;
    
    @ExcelProperty("投诉数量")
    private Integer complaintCount;
    
    @ExcelProperty("联系人")
    private String contactPerson;
    
    @ExcelProperty("联系电话")
    private String contactPhone;
}