// 출력 text만 변경했습니다!
package view;

import view.AppointmentRecordsql;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class AppointmentRecordGUI extends JFrame {
    private JPanel contentPane;
    private JTable table;
    private DefaultTableModel tableModel;
    private int userId;

    public AppointmentRecordGUI(int userId) {
        this.userId = userId;
        setTitle("상담 기록 관리 시스템");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 1200, 800);
        contentPane = new JPanel();
        contentPane.setBackground(new Color(245, 245, 245));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel titleLabel = new JLabel("상담 기록 관리 시스템");
        titleLabel.setFont(new Font("한컴 말랑말랑 Regular", Font.BOLD, 32));
        titleLabel.setBounds(400, 10, 500, 50);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(titleLabel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBounds(30, 80, 1120, 50);
        buttonPanel.setLayout(new GridLayout(1, 6, 10, 0));
        buttonPanel.setOpaque(false);
        contentPane.add(buttonPanel);

        JButton btnLoad = createButton("상담 기록 조회", e -> loadRecords());
        JButton btnCreate = createButton("상담 기록 등록", e -> createRecord());
        JButton btnUpdate = createButton("상담 기록 수정", e -> updateRecord());
        JButton btnDelete = createButton("상담 기록 삭제", e -> deleteRecord());
        JButton btnTrack = createButton("내원자 트래킹 조회", e -> getTrackingRecords());
        JButton btnClose = createButton("닫기", e -> dispose());

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(30, 150, 1120, 600);
        contentPane.add(scrollPane);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(40);
        scrollPane.setViewportView(table);

        if (userId < 20000) { // 환자
            loadRecords();
        } else { // 의료인
            buttonPanel.add(btnLoad);
            buttonPanel.add(btnCreate);
            buttonPanel.add(btnUpdate);
            buttonPanel.add(btnDelete);
            buttonPanel.add(btnTrack);
            buttonPanel.add(btnClose);
        }
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("한컴 말랑말랑 Regular", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.addActionListener(listener);
        return btn;
    }

    public void loadRecords() {
        Object[][] data = AppointmentRecordsql.getRecordsByUser(userId);
        setTableColumns(new String[]{"기록ID", "환자ID", "기관ID", "날짜", "처방", "진단", "내용"});
        updateTable(data);
    }

    public void createRecord() {
        JTextField pidField = new JTextField();
        JTextField diagField = new JTextField();
        JTextField prescField = new JTextField();
        JTextField recordField = new JTextField();

        Object[] message = {
            "환자 ID:", pidField,
            "진단명:", diagField,
            "처방:", prescField,
            "상담 내용:", recordField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "상담 기록 등록", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int pid = Integer.parseInt(pidField.getText());
            String diag = diagField.getText().trim();
            String presc = prescField.getText().trim();
            String rec = recordField.getText().trim();

            boolean result = AppointmentRecordsql.insertRecord(userId, pid, presc, diag, rec);
            JOptionPane.showMessageDialog(this, result ? "등록 완료!" : "등록 실패 - 알레르기 충돌 확인");
        }
    }

    public void updateRecord() {
        JTextField idField = new JTextField();
        JTextField diagField = new JTextField();
        JTextField prescField = new JTextField();
        JTextField recordField = new JTextField();

        Object[] message = {
            "기록ID:", idField,
            "진단명:", diagField,
            "처방:", prescField,
            "상담 내용:", recordField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "기록 수정", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int id = Integer.parseInt(idField.getText());
            String diag = diagField.getText();
            String presc = prescField.getText();
            String rec = recordField.getText();

            boolean result = AppointmentRecordsql.updateRecord(id, diag, presc, rec);
            JOptionPane.showMessageDialog(this, result ? "수정 완료!" : "수정 실패 - 알레르기 충돌 확인");
        }
    }

    public void deleteRecord() {
        String idStr = JOptionPane.showInputDialog(this, "삭제할 상담 기록 ID:");
        if (idStr != null) {
            int id = Integer.parseInt(idStr);
            boolean result = AppointmentRecordsql.deleteRecord(id);
            JOptionPane.showMessageDialog(this, result ? "삭제 완료!" : "삭제 실패");
        }
    }

    public void getTrackingRecords() {
        String input = JOptionPane.showInputDialog(this, "조회할 내원자 ID를 입력하세요.: ");
        if (input == null || input.isEmpty()) return;

        int patientId = Integer.parseInt(input);
        Object[][] data = AppointmentRecordsql.getTrackingRecords(userId, patientId);

        if (data == null) {
            JOptionPane.showMessageDialog(this, "해당 내원자 조회 권한이 없거나 회원 정보가 없습니다.");
        } else if (data.length == 0) {
            JOptionPane.showMessageDialog(this, "해당 사용자의 트래킹 기록이 없습니다.");
        } else {
            setTableColumns(new String[]{"날짜", "기관ID", "감정점수", "수면시간", "운동", "운동시간", "한 마디"});
            updateTable(data);
        }
    }

    private void setTableColumns(String[] columns) {
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
    }

    private void updateTable(Object[][] data) {
        tableModel.setRowCount(0);
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String input = JOptionPane.showInputDialog(null, "사용자 ID를 입력하세요. :");
            if (input != null) {
                try {
                    int userId = Integer.parseInt(input);
                    new AppointmentRecordGUI(userId).setVisible(true);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "유효한 숫자를 입력해주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
