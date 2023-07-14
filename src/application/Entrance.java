package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.RecursiveAction;

import db.JdbcUtil;

public class Entrance {
	private Connection con = JdbcUtil.getCon();
	private Scanner sc = new Scanner(System.in);

	public void entrance() {
		while(true) {
			System.out.println();
			System.out.println("======================================");
			System.out.println("1.�α����ϱ�   2.ȸ������   3.���α׷�����");
			System.out.print("���Ͻô� �׸��� �Է��ϼ���: ");
			try {
				int user_input = sc.nextInt();
				System.out.println();
				switch(user_input) {
					case 1:
						login();
						break;
						
					case 2:
						signup();
						break;
						
					case 3:
						return;
						
					default:
						System.out.println("�߸��� ���ڸ� �Է��ϼ̽��ϴ�. �ٽ� �Է����ּ���.");
						continue;
				}
			}catch(InputMismatchException e) {
				System.out.println("�߸��� ���ڸ� �Է��ϼ̽��ϴ�. �ٽ� �Է����ּ���.");
				sc.nextLine();
				continue;		
			}
		}
	}
	
	public void login() {
		//�α��� ����� �Է¹ޱ�
		System.out.println("<�α���>");
		System.out.print("���̵� �Է��ϼ���: ");
		String id = sc.next();
		System.out.print("��й�ȣ�� �Է��ϼ���: ");
		String pwd = sc.next();
		try {
			//�α��� �˻�
			if(login_check(id, pwd)) {
				//�α��� ����
				CoreApi core = new CoreApi(id, con);
				//���� CoreApi �������� �Ѿ
				core.start();
				System.out.println();
				System.out.println("<�α׾ƿ� �Ǿ����ϴ�>");
				System.out.println("<������������ �̵��մϴ�>");
			}
			//�α��� ����
			else {
				System.out.println("<�α��ν���! �ùٸ��� ���� ���̵�� ��й�ȣ�Դϴ�. �ٽ� �õ����ּ���>");
			}
		}catch(SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("<�α��ΰ������� ������ �߻��߽��ϴ�. ó������ ���ư��ϴ�>");
		}
	}
	
	public void signup() {
			PreparedStatement pstmt = null;
		try {
			//LMEMBER Insert�� ó��(ȸ������)
			String sql = "Insert into LMEMBER Values(LMEM_SEQ.NEXTVAL,?,?,?,?)";
			pstmt = con.prepareStatement(sql);
			System.out.println("<ȸ�������� �����մϴ�>");
			System.out.print("ȸ������ �̸��� �Է����ּ���: ");
			String name = sc.next();
			System.out.print("����Ͻ� ȸ��ID�� �Է����ּ���: ");
			String id = sc.next();
			//���̵� �ߺ����� Ȯ��
			while(duplicate_check(id)) {
				System.out.println("<�ش� ���̵�� �̹� ������� ID�Դϴ�. �ٸ� ID�� ������ּ���>");
				System.out.print("����Ͻ� ȸ��ID�� �Է����ּ���: ");
				id = sc.next();
			}
			System.out.print("��й�ȣ�� �Է����ּ���: ");
			String pwd = sc.next();
			System.out.print("ȸ������ ��ȭ��ȣ�� �Է����ּ���: ");
			String phone = sc.next();
			pstmt.setString(1, name);
			pstmt.setString(2, id);
			pstmt.setString(3, pwd);
			pstmt.setString(4, phone);
			int n = pstmt.executeUpdate();
			if(n == 1) System.out.println("<ȸ�����Կ� �����ϼ̽��ϴ�! �ٽ� �α������ּ���>");
			else System.out.println("<������ �߰ߵǾ� ȸ�������� ��ҵǾ����ϴ�. �ٽ� �õ����ּ���>");
		}catch(SQLException e) {
			System.out.println();
			System.out.println(e.getMessage());
			System.out.println("<�Է¿� ������ �߰ߵǾ����ϴ�. ȸ�������� ����մϴ�>");
		}finally {
			JdbcUtil.close((Statement) pstmt);
		}
	}
	
	public boolean duplicate_check(String id) throws SQLException{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "Select * from LMEMBER where ID = ?";
		pstmt = con.prepareStatement(sql);
		pstmt.setString(1, id);
		rs = pstmt.executeQuery();
		if(rs.next()) return true;
		JdbcUtil.close(null, (Statement) pstmt, rs);
		return false;
	}
	
	public boolean login_check(String id, String pwd) throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "Select * from LMEMBER where id = ? and pwd = ?";
		pstmt = con.prepareStatement(sql);
		pstmt.setString(1, id);
		pstmt.setString(2, pwd);
		rs = pstmt.executeQuery();
		if(!rs.next()) return false;
		return true;
	}
}
