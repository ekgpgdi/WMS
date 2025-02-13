package com.dahye.wms.order.service;

import com.dahye.wms.common.domain.ResponseCode;
import com.dahye.wms.common.exception.ExcelParseException;
import com.dahye.wms.order.dto.request.OrderProductRequest;
import com.dahye.wms.order.dto.request.OrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.usermodel.*;
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

    public long parseNumericCell(Cell cell) {
        if (cell == null) {
            throw new ExcelParseException(ResponseCode.INVALID_NUMBER_FORMAT.toString());
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            return Long.parseLong(cell.getStringCellValue().trim());
        } else {
            throw new ExcelParseException(ResponseCode.INVALID_NUMBER_FORMAT.toString());
        }
    }

    public OrderRequest parseFile(MultipartFile file) {
        List<OrderProductRequest> orderProductRequestList = Lists.newArrayList();
        OrderRequest orderRequest = new OrderRequest();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row firstRow = sheet.getRow(0);

            if (firstRow == null || firstRow.getCell(1) == null) {
                throw new IllegalArgumentException(ResponseCode.REQUIRED_POSTCODE.toString());
            }

            Row secondRow = sheet.getRow(1);
            if (secondRow == null || secondRow.getCell(1) == null) {
                throw new IllegalArgumentException(ResponseCode.REQUIRED_ADDRESS.toString());
            }

            Cell postcodeCell = firstRow.getCell(1);
            Cell addressCell = secondRow.getCell(1);

            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next();
            rowIterator.next();
            rowIterator.next();
            rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                Cell cell1 = row.getCell(0);
                Cell cell2 = row.getCell(1);

                double productId = parseNumericCell(cell1);
                double quantity = parseNumericCell(cell2);

                OrderProductRequest orderProductRequest = new OrderProductRequest();
                orderProductRequest.setProductId((long) productId);
                orderProductRequest.setQuantity((int) quantity);

                orderProductRequestList.add(orderProductRequest);
            }

            orderRequest.setPostcode(String.valueOf(postcodeCell.getStringCellValue()));
            orderRequest.setAddress(String.valueOf(addressCell.getStringCellValue()));
            orderRequest.setOrderProductList(orderProductRequestList);

        } catch (IOException e) {
            log.error("엑셀 처리 중 오류 발생 : " + e.getMessage(), e);
            throw new ExcelParseException(ResponseCode.FILE_ERROR.toString());
        }

        return orderRequest;
    }
}
