package com.dahye.wms.order.service;

import com.dahye.wms.common.exception.ExcelParseException;
import com.dahye.wms.order.dto.request.OrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelService {

    public List<OrderRequest> parseFile(MultipartFile file) {
        List<OrderRequest> orderRequestList = Lists.newArrayList();
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(inputStream);

            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                Cell cell1 = row.getCell(0); // 첫 번째 컬럼
                Cell cell2 = row.getCell(1); // 두 번째 컬럼

                // 데이터 처리 예시
                if (cell1 != null && cell2 != null) {
                    double productId = cell1.getNumericCellValue();
                    double quantity = cell2.getNumericCellValue();

                    OrderRequest orderRequest = new OrderRequest();
                    orderRequest.setProductId((long) productId);
                    orderRequest.setQuantity((int) quantity);

                    orderRequestList.add(orderRequest);
                }
            }

            workbook.close(); // 엑셀 파일 닫기

        } catch (IOException e) {
            log.error("엑셀 처리 중 오류 발생 : " + e.getMessage());
            throw new ExcelParseException("FILE_ERROR");
        }

        return orderRequestList;
    }
}
