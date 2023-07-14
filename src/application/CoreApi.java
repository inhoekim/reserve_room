package application;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import db.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

public class CoreApi {
	Connection con = null;
	private String id;
	private int mid;
	private String name;
	private String date;
	Scanner sc = new Scanner(System.in);
	int library_id = 1;
	String library_name = "������";
	
	public CoreApi(String id, Connection con) throws SQLException{
		this.id = id;
		this.con = con;
		//LMEMBER���̺��� MNAME Į���� ��������
		String sql = "Select MID, MNAME from LMEMBER where id = ?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, id);
		ResultSet rs = pstmt.executeQuery();
		rs.next(); 
		this.name = rs.getString("MNAME");
		this.mid = rs.getInt("MID");
		SimpleDateFormat sdft = new SimpleDateFormat("<�α��� �ð�: yyyy-MM-dd a HH:mm:ss (E)>\n");
		SimpleDateFormat sdft2 = new SimpleDateFormat("yy/MM/dd");
		this.date = sdft2.format(new Date());
		//�α��� �λ� �����
		String greeting = "<�α��� ����!>\n"+ sdft.format(new Date()) + "�ݰ����ϴ�. " + name + "��!";
		System.out.println();
		System.out.println(greeting);
		JdbcUtil.close(null,(Statement) pstmt, rs);
	}
	
	public void start() {
		while(true) {
			//Admin ����
			if (id.equals("admin")) {
				System.out.println();
				System.out.println("=========================================================");
				System.out.println("<����! �� ������ �����������Դϴ�>");
				System.out.println("1.��������������  2.ȸ������Ȯ��  3.�����������߰�  4.���͵����  5.����");
				System.out.println("(���絵���� Page - " + library_name + "����)");
				System.out.print("���Ͻô� �׸��� �Է��ϼ���: ");
				int user_num= sc.nextInt();
				CoreAdmin admin = new CoreAdmin(con);
				try {
					switch(user_num) {
						case 1:
							change_lib();
							break;
							
						case 2:
							admin.print_member();
							break;
						
						case 3:
							admin.insert_library();
							break;
							
						case 4:
							admin.insert_libRoom(library_id, library_name);
							break;
							
						case 5:
							return;
							
						default:
							System.out.println("�߸��� �׸��� �Է��ϼ̽��ϴ�. �ٽ� �Է����ּ���");
					}
				}catch(SQLException e) {
					System.out.println(e.getMessage());
					System.out.println("<�Է¿� ������ �߰ߵǾ����ϴ�. �������� ���ư��ϴ�>");
				}
			//Member ����
			}else {

				System.out.println();
				System.out.println("============================================================");
				System.out.println("1.��������������  2.���Ͽ����ȲȮ��  3.���͵�뿹��  4.���ǿ�����  5.����");
				System.out.println("(���絵���� Page - " + library_name + "����)");
				System.out.print("���Ͻô� �׸��� �Է��ϼ���: ");
				int user_num= sc.nextInt();
				CoreMem mem = new CoreMem(con);
				try {
					switch(user_num) {
						case 1:
							change_lib();
							break;
							
						case 2:
							mem.select_reservation(library_id, library_name);
							break;
							
						case 3:
							
							HashMap<Integer, Boolean[]> tt = mem.select_reservation(library_id, library_name);
							mem.insert_reservation(library_id, mid, tt);
							break;
							
						case 4:
							mem.select_myReservation(mid, date);
							break;
							
						case 5:
							return;
							
						default:
							System.out.println("�߸��� �׸��� �Է��ϼ̽��ϴ�. �ٽ� �Է����ּ���");
					}
				}catch(SQLException e) {
					e.printStackTrace();
					System.out.println("<�Է¿� ������ �߰ߵǾ����ϴ�. �������� ���ư��ϴ�>");
					System.out.println(e.getMessage());
				}
			}
		}
	}
	
	public void change_lib() throws SQLException {
		CoreMem mem = new CoreMem(con);
		//DB���� ������ ��� ��������
		HashMap<Integer, String> map = mem.get_library();
		System.out.println("<���� ������ ���� ��� List>");
		if(map.isEmpty()) {
			System.out.println("��ϵ� �������� �������� �ʽ��ϴ�");
			return;
		}
		//����ڿ��� ������ ������ ��� String �����
		String list = "";
		for(Entry<Integer, String> s : map.entrySet()) {
			list += s.getKey() + "." + s.getValue() + " ";
		}
		//����� �Է¹ޱ�
		while(true){
			System.out.println(list);
			System.out.println();
			System.out.print("�����ϰ� ���� ������ ��ȣ�� �Է� : ");
			try {
				Set<Integer> s = map.keySet();
				int userNum = sc.nextInt();
				if(s.contains(userNum)) {
					this.library_id = userNum;
					this.library_name = map.get(userNum);
					break;
				}else {
					System.out.println("��ȣ�� �߸� �Է��ϼ̽��ϴ�. �ٽ� �Է����ּ���");
					System.out.println();
					continue;
				}
			}catch(InputMismatchException e) {
				System.out.println("��ȣ�� �߸� �Է��ϼ̽��ϴ�. �ٽ� �Է����ּ���");
				System.out.println();
				continue;
			}
		}		
	}
}
