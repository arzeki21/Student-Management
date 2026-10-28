package com.example.mid;

import android.os.Bundle;
import android.database.Cursor;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText etStudentId, etName, etAge;
    Button btnAdd, btnView, btnEdit, btnDelete;
    ListView listView;

    DBHelper dbHelper;
    ArrayList<String> studentList;
    ArrayList<Long> studentIds;
    ArrayAdapter<String> adapter;

    long selectedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        etStudentId = findViewById(R.id.etStudentId);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);

        btnAdd = findViewById(R.id.btnAdd);
        btnView = findViewById(R.id.btnView);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        listView = findViewById(R.id.listViewStudents);

        studentList = new ArrayList<>();
        studentIds = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                studentList
        );
        listView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> addStudent());
        btnView.setOnClickListener(v -> viewStudents());
        btnEdit.setOnClickListener(v -> editStudent());
        btnDelete.setOnClickListener(v -> deleteStudent());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedId = studentIds.get(position);

            String[] parts = studentList.get(position).split("\\|", -1);
            if (parts.length >= 3) {
                etStudentId.setText(parts[1].trim());
                etName.setText(parts[0].trim());
                etAge.setText(parts[2].trim());
            }
        });

        viewStudents();
    }

    private boolean validateInput() {
        String studentId = etStudentId.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String ageText = etAge.getText().toString().trim();

        if (studentId.isEmpty() || name.isEmpty() || ageText.isEmpty()) {
            Toast.makeText(this, "Semua kolom wajib diisi",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int age = Integer.parseInt(ageText);
            if (age < 17 || age > 50) {
                Toast.makeText(this, "Usia harus 17 sampai 50 tahun",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Usia harus berupa angka",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void addStudent() {
        if (!validateInput()) return;

        boolean success = dbHelper.addStudent(
                etStudentId.getText().toString().trim(),
                etName.getText().toString().trim(),
                Integer.parseInt(etAge.getText().toString().trim())
        );

        if (success) {
            Toast.makeText(this, "Data berhasil ditambahkan",
                    Toast.LENGTH_SHORT).show();
            clearFields();
            viewStudents();
        } else {
            Toast.makeText(this, "Gagal: Student ID mungkin sudah digunakan",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void viewStudents() {
        studentList.clear();
        studentIds.clear();

        Cursor cursor = dbHelper.getAllStudents();

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(
                            cursor.getColumnIndexOrThrow(DBHelper.COL_ID));
                    String studentId = cursor.getString(
                            cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_ID));
                    String name = cursor.getString(
                            cursor.getColumnIndexOrThrow(DBHelper.COL_NAME));
                    int age = cursor.getInt(
                            cursor.getColumnIndexOrThrow(DBHelper.COL_AGE));

                    studentIds.add(id);
                    studentList.add(name + " | " + studentId + " | " + age);
                }
            } finally {
                cursor.close();
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void editStudent() {
        if (selectedId == -1) {
            Toast.makeText(this, "Pilih data dari daftar terlebih dahulu",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateInput()) return;

        boolean success = dbHelper.updateStudent(
                selectedId,
                etStudentId.getText().toString().trim(),
                etName.getText().toString().trim(),
                Integer.parseInt(etAge.getText().toString().trim())
        );

        if (success) {
            Toast.makeText(this, "Data berhasil diperbarui",
                    Toast.LENGTH_SHORT).show();
            clearFields();
            viewStudents();
        } else {
            Toast.makeText(this, "Gagal memperbarui data; periksa Student ID",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void deleteStudent() {
        if (selectedId == -1) {
            Toast.makeText(this, "Pilih data yang ingin dihapus",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.deleteStudent(selectedId);

        if (success) {
            Toast.makeText(this, "Data berhasil dihapus",
                    Toast.LENGTH_SHORT).show();
            clearFields();
            viewStudents();
        } else {
            Toast.makeText(this, "Data gagal dihapus",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        etStudentId.setText("");
        etName.setText("");
        etAge.setText("");
        selectedId = -1;
        listView.clearChoices();
    }
}