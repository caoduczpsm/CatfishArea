package com.example.catfisharea.ultilities;

import android.net.Uri;
import android.os.Environment;

import com.example.catfisharea.listeners.ExcelListener;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelHandler {
    private Workbook workbook;

    public ExcelHandler(String filePath) {
        this.workbook = getWorkbookFromFilePath(filePath);
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }
//    Lấy đối tượng workbook để làm việc với excel
    private Workbook getWorkbookFromFilePath(String filePath) {
        try {
            // Tạo đối tượng FileInputStream để đọc tệp Excel
            FileInputStream file = new FileInputStream(new File(filePath));
            // Tạo đối tượng Workbook để đại diện cho tệp Excel

            Workbook workbook = WorkbookFactory.create(file);

            // Đóng tệp Excel
            file.close();
            return workbook;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
// Lấy sheet theo tên sheet
    public Sheet getSheetAtName(String sheetName) {
        return workbook.getSheet(sheetName);
    }

//    Lấy sheet theo index
    public Sheet getSheetAtIndex(int index) {
        return workbook.getSheetAt(index);
    }

    //    Dọc sheet và sử lý dữ liệu trong ô bằng cách thêm logic vào hàm
    public void readDataFromSheet(Sheet sheet, ExcelListener excelListener) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                excelListener.handlerCell(cell);
            }
        }
    }
//    Đọc sheet và trả về mảng dữ liệu theo dòng
    public List<List<String>> readDataSheet(Sheet sheet) {
        List<List<String>> resultData = new ArrayList<>();
        for (Row row : sheet) {
            List<String> rowData = new ArrayList();
            for (Cell cell : row) {
                rowData.add(cell.toString());
            }
            resultData.add(rowData);
        }
        return resultData;
    }

    public String[] getTitle(Sheet sheet) {
        List<String> result = new ArrayList<>();
        Row row = sheet.getRow(0);
        for (Cell cell: row) {
            result.add(cell.toString());
        }
        return result.toArray(new String[0]);
    }

    public String[] getContent(Sheet sheet) {
        List<String> result = new ArrayList<>();
        sheet.removeRow(sheet.getRow(0));
        for (Row row: sheet) {
            for (Cell cell: row) {
                result.add(cell.toString());
            }
        }
        return result.toArray(new String[0]);
    }

//    Lấy real Path file
    public static String getFilePathFromUri(Uri uri) {
        String[] filename1;
        String fn;
        String filepath = uri.getPath();
        String filePath1[] = filepath.split(":");
        filename1 = filepath.split("/");
        fn = filename1[filename1.length - 1];
        return Environment.getExternalStorageDirectory().getPath() + "/" + filePath1[1];
    }

// Cách sử dụng:
//    - Set sự kiện chọn file
//    private void openFileChooser() {
//        if (SDK_INT >= Build.VERSION_CODES.R) {
//            if (Environment.isExternalStorageManager()) {
//
//                Intent intent = new Intent();
//                intent.setType("*/*");
//                intent.putExtra(Intent.EXTRA_AUTO_LAUNCH_SINGLE_CHOICE, true);
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Excel File "), 101);
//            } else {
//
//                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                Uri uri = Uri.fromParts("package", getPackageName(), null);
//                startActivity(intent);
//            }
//        } else {
//
//
//            Intent intent = new Intent();
//            intent.setType("*/*");
//            intent.putExtra(Intent.EXTRA_AUTO_LAUNCH_SINGLE_CHOICE, true);
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 102);
//        }
//    }

    //    Lấy file Path
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 101 && data != null) {
//            Uri fileuri = data.getData();
//            String filePath = ExcelHandler.getFilePathFromUri(fileuri);
//
////              Tạo excel Handler để thao tác file Excel
//            ExcelHandler excelHandler = new ExcelHandler(filePath);
//
////              Các hàm sử lý
//
//        }
//    }
}
