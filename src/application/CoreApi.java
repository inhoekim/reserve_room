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
	String library_name = "고양원흥";
	
	public CoreApi(String id, Connection con) throws SQLException{
		this.id = id;
		this.con = con;
		//LMEMBER테이블에서 MNAME 칼럼값 가져오기
		String sql = "Select MID, MNAME from LMEMBER where id = ?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, id);
		ResultSet rs = pstmt.executeQuery();
		rs.next(); 
		this.name = rs.getString("MNAME");
		this.mid = rs.getInt("MID");
		SimpleDateFormat sdft = new SimpleDateFormat("<로그인 시간: yyyy-MM-dd a HH:mm:ss (E)>\n");
		SimpleDateFormat sdft2 = new SimpleDateFormat("yy/MM/dd");
		this.date = sdft2.format(new Date());
		//로그인 인삿말 만들기
		String greeting = "<로그인 성공!>\n"+ sdft.format(new Date()) + "반갑습니다. " + name + "님!";
		System.out.println();
		System.out.println(greeting);
		JdbcUtil.close(null,(Statement) pstmt, rs);
	}
	
	public void start() {
		while(true) {
			//Admin 전용
			if (id.equals("admin")) {
				System.out.println();
				System.out.println("=========================================================");
				System.out.println("<주의! 이 세션은 관리자전용입니다>");
				System.out.println("1.도서관지점변경  2.회원정보확인  3.도서관지점추가  4.스터디룸등록  5.종료");
				System.out.println("(현재도서관 Page - " + library_name + "지점)");
				System.out.print("원하시는 항목을 입력하세요: ");
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
							System.out.println("잘못된 항목을 입력하셨습니다. 다시 입력해주세요");
					}
				}catch(SQLException e) {
					System.out.println(e.getMessage());
					System.out.println("<입력에 오류가 발견되었습니다. 메인으로 돌아갑니다>");
				}
			//Member 전용
			}else {

				System.out.println();
				System.out.println("============================================================");
				System.out.println("1.도서관지점변경  2.금일예약상황확인  3.스터디룸예약  4.나의예약기록  5.종료");
				System.out.println("(현재도서관 Page - " + library_name + "지점)");
				System.out.print("원하시는 항목을 입력하세요: ");
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
							System.out.println("잘못된 항목을 입력하셨습니다. 다시 입력해주세요");
					}
				}catch(SQLException e) {
					e.printStackTrace();
					System.out.println("<입력에 오류가 발견되었습니다. 메인으로 돌아갑니다>");
					System.out.println(e.getMessage());
				}
			}
		}
	}
	
	public void change_lib() throws SQLException {
		CoreMem mem = new CoreMem(con);
		//DB에서 도서관 목록 가져오기
		HashMap<Integer, String> map = mem.get_library();
		System.out.println("<고양시 도서관 지점 목록 List>");
		if(map.isEmpty()) {
			System.out.println("등록된 도서관이 존재하지 않습니다");
			return;
		}
		//사용자에게 보여줄 도서관 목록 String 만들기
		String list = "";
		for(Entry<Integer, String> s : map.entrySet()) {
			list += s.getKey() + "." + s.getValue() + " ";
		}
		//사용자 입력받기
		while(true){
			System.out.println(list);
			System.out.println();
			System.out.print("변경하고 싶은 도서관 번호를 입력 : ");
			try {
				Set<Integer> s = map.keySet();
				int userNum = sc.nextInt();
				if(s.contains(userNum)) {
					this.library_id = userNum;
					this.library_name = map.get(userNum);
					break;
				}else {
					System.out.println("번호를 잘못 입력하셨습니다. 다시 입력해주세요");
					System.out.println();
					continue;
				}
			}catch(InputMismatchException e) {
				System.out.println("번호를 잘못 입력하셨습니다. 다시 입력해주세요");
				System.out.println();
				continue;
			}
		}		
	}
}
