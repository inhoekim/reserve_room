package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.Scanner;

import db.JdbcUtil;

public class CoreAdmin {
	Connection con = null;
	Scanner sc = new Scanner(System.in);
	
	public CoreAdmin(Connection con) {
		this.con = con;
	}
	//1.ȸ������Ȯ��
	public void print_member() throws SQLException{
		String sql1 = "Select * from LMEMBER";
		String sql2 = "Select * from LMEMBER Where id = ?";
		System.out.println();
		System.out.println("1.ȸ����ü����  2.Ư��ȸ���˻�");
		System.out.print("���Ͻô� �׸��� �Է��ϼ���: ");
		int intInput;
		PreparedStatement pstmt = null;
		while(true) {
			try {
				intInput = sc.nextInt();
				if(intInput == 1) {
					pstmt = con.prepareStatement(sql1);
					break;
				}else if(intInput == 2) {
					System.out.print("�˻��� ȸ���� ���̵� �Է����ּ���: ");
					pstmt = con.prepareStatement(sql2);
					sc.nextLine();
					String strInput = sc.nextLine();
					pstmt.setString(1,  strInput);
					break;
				}else{
					System.out.println("�߸��� �Է��Դϴ�. �ٽ� �Է����ּ���");
					System.out.println();
					continue;
				}
			}catch(InputMismatchException e) {
				System.out.println("�߸��� �Է��Դϴ�. �ٽ� �Է����ּ���");
				System.out.println();
				sc.nextLine();
				continue;
			}
		}
		ResultSet rs = pstmt.executeQuery();
		System.out.println();
		if(!rs.next()) {
			System.out.println("��ϵ� ȸ���� �������� �ʽ��ϴ�.");
		}else {
			do {
				int mnum = rs.getInt("MID");
				String id = rs.getString("ID"), mname = rs.getString("MNAME"), mphone = rs.getString("MPHONE");
				System.out.println("(" + mnum + "��) �̸�:" + mname + ", ���̵�:" + id + ", ��ȭ��ȣ:" + mphone);
			}while(rs.next());
		}
		JdbcUtil.close(null, (Statement) pstmt, rs);
	}
	//2.�������
	public void insert_library() throws SQLException{
		String sql = "Insert into LIBRARY Values(LIBRARY_SEQ.NEXTVAL, ?, ?, ?)";
		System.out.print("�߰��� ���������� �̸�: ");
		String libName = sc.nextLine();
		System.out.print("�߰��� ���������� �ּ���: ");
		String libLoc = sc.nextLine();
		System.out.print("�߰��� ���������� ��ȭ��ȣ: ");
		String libPhone = sc.nextLine();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, libName);
		pstmt.setString(2, libLoc);
		pstmt.setString(3, libPhone);
		pstmt.executeQuery();
		JdbcUtil.close(null, (Statement) pstmt, null);
		System.out.println("<���������� �۾��� �Ϸ��߽��ϴ�>");
	}
	//3.���͵����
	public void insert_libRoom(int library_id, String library_name) throws SQLException {
		System.out.println();
		System.out.println("<" + library_name + "������ ���ο� ���͵���� �߰��մϴ�>");
		int capacity, pc;
		String roomName;
		while(true) {
			try {
				System.out.print("�߰��� ���� �̸� �Է�: ");
				roomName = sc.nextLine();
				System.out.print("�߰��� ���� ���밡���� �ο��� �Է�: ");
				capacity = sc.nextInt();
				System.out.print("�߰��� �濡�� �̿밡���� PC���� �Է�: ");
				pc = sc.nextInt();
				break;
			}catch(InputMismatchException e) {
				System.out.println("<�߸��� �Է��Դϴ�. ó������ �ٽ� �Է��մϴ�>");
				sc.nextLine();
				continue;
			}
		}
		String sql = "Insert into LROOM Values(LROOM_SEQ.NEXTVAL, ?, ?, ?, ?)";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setInt(1, library_id);
		pstmt.setString(2, roomName);
		pstmt.setInt(3, capacity);
		pstmt.setInt(4, pc);
		pstmt.executeUpdate();
		JdbcUtil.close(null, (Statement) pstmt, null);
		System.out.println("<���������� �۾��� �Ϸ��߽��ϴ�>");
	}
	
}
