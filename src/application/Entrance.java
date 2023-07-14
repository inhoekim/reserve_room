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
			System.out.println("1.로그인하기   2.회원가입   3.프로그램종료");
			System.out.print("원하시는 항목을 입력하세요: ");
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
						System.out.println("잘못된 숫자를 입력하셨습니다. 다시 입력해주세요.");
						continue;
				}
			}catch(InputMismatchException e) {
				System.out.println("잘못된 숫자를 입력하셨습니다. 다시 입력해주세요.");
				sc.nextLine();
				continue;		
			}
		}
	}
	
	public void login() {
		//로그인 사용자 입력받기
		System.out.println("<로그인>");
		System.out.print("아이디를 입력하세요: ");
		String id = sc.next();
		System.out.print("비밀번호를 입력하세요: ");
		String pwd = sc.next();
		try {
			//로그인 검사
			if(login_check(id, pwd)) {
				//로그인 성공
				CoreApi core = new CoreApi(id, con);
				//이후 CoreApi 로직으로 넘어감
				core.start();
				System.out.println();
				System.out.println("<로그아웃 되었습니다>");
				System.out.println("<시작페이지로 이동합니다>");
			}
			//로그인 실패
			else {
				System.out.println("<로그인실패! 올바르지 않은 아이디와 비밀번호입니다. 다시 시도해주세요>");
			}
		}catch(SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("<로그인과정에서 오류가 발생했습니다. 처음으로 돌아갑니다>");
		}
	}
	
	public void signup() {
			PreparedStatement pstmt = null;
		try {
			//LMEMBER Insert문 처리(회원가입)
			String sql = "Insert into LMEMBER Values(LMEM_SEQ.NEXTVAL,?,?,?,?)";
			pstmt = con.prepareStatement(sql);
			System.out.println("<회원가입을 진행합니다>");
			System.out.print("회원님의 이름을 입력해주세요: ");
			String name = sc.next();
			System.out.print("사용하실 회원ID를 입력해주세요: ");
			String id = sc.next();
			//아이디 중복여부 확인
			while(duplicate_check(id)) {
				System.out.println("<해당 아이디는 이미 사용중인 ID입니다. 다른 ID를 사용해주세요>");
				System.out.print("사용하실 회원ID를 입력해주세요: ");
				id = sc.next();
			}
			System.out.print("비밀번호를 입력해주세요: ");
			String pwd = sc.next();
			System.out.print("회원님의 전화번호를 입력해주세요: ");
			String phone = sc.next();
			pstmt.setString(1, name);
			pstmt.setString(2, id);
			pstmt.setString(3, pwd);
			pstmt.setString(4, phone);
			int n = pstmt.executeUpdate();
			if(n == 1) System.out.println("<회원가입에 성공하셨습니다! 다시 로그인해주세요>");
			else System.out.println("<오류가 발견되어 회원가입이 취소되었습니다. 다시 시도해주세요>");
		}catch(SQLException e) {
			System.out.println();
			System.out.println(e.getMessage());
			System.out.println("<입력에 오류가 발견되었습니다. 회원가입을 취소합니다>");
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
