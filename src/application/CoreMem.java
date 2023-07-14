package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.naming.spi.DirStateFactory.Result;

import db.JdbcUtil;

public class CoreMem {
	Connection con = null;
	
	public CoreMem(Connection con) {
		this.con = con;
	}
	//1.금일예약확인
	public HashMap<Integer, Boolean[]> select_reservation(int library_id, String library_name) throws SQLException {
		HashMap<Integer, Boolean[]> reserved_roomList = new HashMap<>();
		//현재 날짜 및 시간 구하기
		SimpleDateFormat sdft = new SimpleDateFormat("yy/MM/dd HH");
		Date date = new Date();
		String time[] = sdft.format(date).split(" ");
		//현재 바라보고 있는 LIBRARY에 속하는 모든 LROOM.RID 구하기
		int[] ridList = null;
		ridList = get_ridList(library_id);
		for(int rid : ridList) {
			Boolean[] timetable = new Boolean[10]; // timetable[0] => 10-11 ... timetable[9] => 19-20
			//현재 시간을 계산하여 현재 시간보다 앞에 있는 timetable들은 모두 false처리
			for(int i = 0; i < 10; i++) {
				int now = Integer.parseInt(time[1]);
				if((i+10) < now) timetable[i] = false;
				else timetable[i] = true;
			}
			//이미 예약된 timetable들을 false 처리
			String sql = "SELECT VSTARTTIME, VENDTIME FROM RESERVATIONS WHERE TO_CHAR(VDATE, 'YY/MM/DD') = ? AND RID = ?";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, time[0]);
			pstmt.setInt(2, rid);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				int vstarttime = rs.getInt("VSTARTTIME"), vendtime = rs.getInt("VENDTIME");
				int var = vendtime - vstarttime;
				if(var == 1) {
					timetable[vstarttime - 10] = false;
				}else if(var == 2) {
					timetable[vstarttime - 10] = false;
					timetable[vstarttime - 9] = false;
				}
			}
			reserved_roomList.put(rid, timetable);
		}
		print_reservation(reserved_roomList, time[0], library_name);
		return reserved_roomList;
	}
	//2.스터디룸예약
	public void insert_reservation(int library_id, int mid, HashMap<Integer, Boolean[]> reserved_roomList) throws SQLException{
		//사용자에게 보여줄 화면만들기
		String list = "";
		for(Entry<Integer, Boolean[]> entry: reserved_roomList.entrySet()) {
			String sql = "select RNAME from LROOM where rid = ?";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, entry.getKey());
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			String roomName = rs.getString("RNAME");
			list += entry.getKey() + "." + roomName + "  ";
			JdbcUtil.close(null, (Statement)pstmt, rs);
		}
		//사용자 인풋받기
		int userNum;
		while(true) {
			System.out.println();
			System.out.println(list);
			System.out.print("예약하고 싶은 스터디룸 번호입력 : ");
			try {
				Scanner sc = new Scanner(System.in);
				userNum = sc.nextInt();
				if(!reserved_roomList.keySet().contains(userNum)) {
					System.out.println("<잘못된 입력이 발견되었습니다. 다시 입력해주세요>");
					continue;
				}
				//사용자가 정한 스터디룸 예약진행
				System.out.println();
				System.out.print("예약하고 싶은 시간대 입력: ");
				int wanted_time = sc.nextInt();
				if (wanted_time < 10 || wanted_time >19) {
					System.out.println("<잘못된 입력이 발견되었습니다. 다시 입력해주세요>");
					continue;
				}
				Boolean[] tt = reserved_roomList.get(userNum);
				if(!tt[wanted_time-10]) {
					System.out.println("해당 시간대는 이미 예약되어져 있습니다. 다른 시간대를 골라주세요.");
					continue;
				}
				//예약 sql문 작성
				String sql = "Insert into RESERVATIONS values(RESERVE_SEQ.NEXTVAL, ?, ?, SYSDATE, ?, ?)";
				PreparedStatement pstmt = con.prepareStatement(sql);
				pstmt.setInt(1, mid);
				pstmt.setInt(2, userNum);
				pstmt.setInt(3, wanted_time);
				pstmt.setInt(4, wanted_time + 1);
				if(tt[wanted_time-9]) {
					System.out.println("몇시간 예약하시겠습니까? 최대 2시간 연속까지 사용가능합니다.\n1.1시간  2.2시간");
					int howMany = sc.nextInt();
					if(howMany == 1) pstmt.setInt(4, wanted_time + 1);
					else if(howMany == 2) pstmt.setInt(4, wanted_time + 2);
					else{
						System.out.println("<잘못된 입력이 발견되었습니다. 다시 입력해주세요>");
						continue;
					}
				}
				pstmt.executeUpdate();
				System.out.println("<작업을 성공적으로 완료하였습니다>");
				break;
			}catch(InputMismatchException e) {
				System.out.println("<잘못된 입력이 발견되었습니다. 다시 입력해주세요>");
				continue;
			}
		}
	}
	//3.도서관 목록가져오기
	public HashMap<Integer,String> get_library() throws SQLException {
		//HashMap<도서관ID,도서관Name> map에 모든 도서관지점 넣기
		HashMap<Integer,String> map = new HashMap<>();
		String sql = "Select LID, LNAME from LIBRARY ORDER BY LID";
		Statement stmt = con.createStatement();
		ResultSet rs= stmt.executeQuery(sql);
		while(rs.next()) {
			map.put(rs.getInt("LID"), rs.getString("LNAME"));
		}
		JdbcUtil.close(null, stmt, rs);
		//libList 리턴
		return map;
	}
	//4.나의예약기록
	public void select_myReservation(int mid, String date) throws SQLException{
		String sql ="SELECT VSTARTTIME, VENDTIME, LNAME, RNAME " +
					"FROM RESERVATIONS, LROOM, LIBRARY " +
					"WHERE RESERVATIONS.RID = LROOM.RID AND LROOM.LID = LIBRARY.LID AND " +
					"TO_CHAR(VDATE) = ? AND MID = ? " +
					"ORDER BY VDATE";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setString(1, date);
		pstmt.setInt(2, mid);
		ResultSet rs = pstmt.executeQuery();
		System.out.println();
		System.out.println("[" + date + "]");
		while(rs.next()) {
			String libName = rs.getString("LNAME"), roomName = rs.getString("RNAME");
			String vstartTime = rs.getString("VSTARTTIME"), vendTime = rs.getString("VENDTIME");
			System.out.println(libName + "지점 " + roomName + " " + vstartTime + "시 ~ " + vendTime + "시 예약");
		}
	}
	//library_id에 속한 rid 리스트 구하기
	public int[] get_ridList(int library_id) throws SQLException{
		String sql = "Select count(rid) as cnt from LROOM where lid = ?";
		PreparedStatement pstmt = con.prepareStatement(sql);
		pstmt.setInt(1, library_id);
		ResultSet rs = pstmt.executeQuery();
		int [] ridList = null;
		if(rs.next()) ridList = new int [rs.getInt("cnt")];
		else return ridList;
		sql = "Select rid from LROOM where lid = ?";
		pstmt = con.prepareStatement(sql);
		pstmt.setInt(1, library_id);
		rs = pstmt.executeQuery();
		int i  = 0;
		while(rs.next()) {
			ridList[i] = rs.getInt("rid");
			i++;
		}
		JdbcUtil.close(null, (Statement) pstmt, rs);
		return ridList;
	}
	// 예약정보출력하는 함수
	public void print_reservation(HashMap<Integer, Boolean[]> reserved_roomList, String date, String library_name) throws SQLException{
		System.out.println();
		System.out.println("===================  "+ date + " "+ library_name + "지점 예약상황표  ===================");
		System.out.println();
		System.out.println("--------------" + "|10-11|11-12|12-13|13-14|14-15|15-16|16-17|17-18|18-19|19-20|");
		for(Entry<Integer, Boolean[]> entry : reserved_roomList.entrySet()) {
			String sql = "select RNAME from LROOM where rid = ?";
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, entry.getKey());
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			String roomName = rs.getString("RNAME");
			Boolean[] timetable = entry.getValue();
			System.out.printf("%-11s|  %c  |  %c  |  %c  |  %c  |  %c  |  %c  |  %c  |  %c  |  %c  |  %c  |\n",
					roomName, pb(timetable[0]), pb(timetable[1]), pb(timetable[2]), pb(timetable[3]), pb(timetable[4]),
					pb(timetable[5]), pb(timetable[6]), pb(timetable[7]), pb(timetable[8]), pb(timetable[9]));
			JdbcUtil.close(null, (Statement)pstmt, rs);
		}
		
	}
	//print_boolean
	public char pb(boolean bool) {
		if(bool) return 'o';
		else return 'x';
	}
}

