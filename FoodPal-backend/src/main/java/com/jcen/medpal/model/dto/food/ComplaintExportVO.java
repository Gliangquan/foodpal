package com.jcen.medpal.model.dto.food;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ColumnWidth(20)
public class ComplaintExportVO {
    
    @ExcelProperty("投诉编号")
    private String complaintNo;
    
    @ExcelProperty("投诉标题")
    private String complaintTitle;
    
    @ExcelProperty("商户")
    private String merchantName;
    
    @ExcelProperty("学生")
    private String studentName;
    
    @ExcelProperty("状态")
    private String status;
    
    @ExcelProperty("处理进度")
    private String processProgress;
    
    @ExcelProperty("整改要求")
    private String rectifyRequirement;
    
    @ExcelProperty("整改结果")
    private String rectifyResult;
    
    @ExcelProperty("反馈")
    private String feedback;
    
    @ExcelProperty("评价")
    private String resultRating;
    
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
    
    @ExcelProperty("处理时间")
    private LocalDateTime processTime;
}