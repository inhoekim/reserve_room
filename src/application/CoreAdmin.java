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
	//1.회원정보확인
	public void print_member() throws SQLException{
		String sql1 = "Select * from LMEMBER";
		String sql2 = "Select * from LMEMBER Where id = ?";
		System.out.println();
		System.out.println("1.회원전체보기  2.특정회원검색");
		System.out.print("원하시는 항목을 입력하세요: ");
		int intInput;
		PreparedStatement pstmt = null;
		while(true) {
			try {
				intInput = sc.nextInt();
				if(intInput == 1) {
					pstmt = con.prepareStatement(sql1);
					break;
				}else if(intInput == 2) {
					System.out.print("검색할 회원의 아이디를 입력해주세요: ");
					pstmt = con.prepareStatement(sql2);
					sc.nextLine();
					String strInput = sc.nextLine();
					pstmt.setString(1,  strInput);
					break;
				}else{
					System.out.println("잘못된 입력입니다. 다시 입력해주세요");
					System.out.println();
					continue;
				}
			}catch(InputMismatchException e) {
				System.out.println("잘못된 입력입니다. 다시 입력해주세요");
				System.out.println();
				sc.nextLine();
				continue;
			}
		}
		ResultSet rs = pstmt.executeQuery();
		System.out.println();
		if(!rs.next()) {
			System.out.println("등록된 회원이 존재하지 않습니다.");
		}else {
			do {
				int mnum = rs.getInt("MID");
				String id = rs.getString("ID"), mname = rs.getString("MNAME"), mphone = rs.getString("MPHONE");
				System.out.println("(" + mnum + "번) 이름:" + mname + ", 아이디:" + id + ", 전화번호:" + mphone);
			}while(rs.next());
		}
		JdbcUtil.close(null, (Statement) pstmt, rs);
	}
	//2.지점등록
	public void insert_library() throws SQLException{
		String sql = "Insert into LIBRARY Values(LIBRARY_SEQ.NEXTVAL, ?, ?, ?)";
		System.out.print("추가할 도서관지점 이름: ");
		String libName = sc.nextLine();
		System.out.print("추가할 도서관지점 주소지: ");
		String libLoc = sc.nextLine();
		System.out.print("추가할 도서관지점 전화번호: ");
		String libPhone = sc.nextLine();
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, libName);
		pstmt.setString(2, libLoc);
		pstmt.setString(3, libPhone);
		pstmt.executeQuery();
		JdbcUtil.close(null, (Statement) pstmt, null);
		System.out.println("<성공적으로 작업을 완료했습니다>");
	}
	//3.스터디룸등록
	public void insert_libRoom(int library_id, String library_name) throws SQLException {
		System.out.println();
		System.out.println("<" + library_name + "지점에 새로운 스터디룸을 추가합니다>");
		int capacity, pc;
		String roomName;
		while(true) {
			try {
				System.out.print("추가할 방의 이름 입력: ");
				roomName = sc.nextLine();
				System.out.print("추가할 방의 수용가능한 인원수 입력: ");
				capacity = sc.nextInt();
				System.out.print("추가할 방에서 이용가능한 PC개수 입력: ");
				pc = sc.nextInt();
				break;
			}catch(InputMismatchException e) {
				System.out.println("<잘못된 입력입니다. 처음부터 다시 입력합니다>");
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
		System.out.println("<성공적으로 작업을 완료했습니다>");
	}
	
}
