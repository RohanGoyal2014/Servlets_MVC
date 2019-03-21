package cleeda;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@WebServlet("/AccountsControllerServlet")
public class AccountsControllerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private UserDBUtil userDBUtil;
	
	@Resource(name="jdbc/cleeda_connection_pool")
	private DataSource dataSource;	

    @Override
	public void init() throws ServletException {
		super.init();
		
		userDBUtil=new UserDBUtil(dataSource);
	}

	public AccountsControllerServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		String command = request.getParameter("command");
		
		if(command==null) {
			response.sendRedirect("login.jsp");
			return;
		}
		
		try {
			switch(command) {
				case "resetPassword":
					resetPasswordManage(request, response);
					break;
				default:
					response.sendRedirect("login.jsp");
			}
		} catch( Exception e) {
			throw new ServletException(e);
		}
		
		
	}

	private void resetPasswordManage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String email = request.getParameter("email");
		
		if(email == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			
			return;	
		}
		email=email.trim();
		boolean error = (validateEmail(email)!=null);
		if(!error) {
			if(!userDBUtil.doesEmailExist(email)) {
				error = true;
			} else if(!userDBUtil.isValidRecovery(email)) {
				error = true;
			}
		}
		
		if(error) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		
		} else {
			RequestDispatcher requestDispatcher = request.getRequestDispatcher("resetPassword.jsp");
			requestDispatcher.forward(request, response);
			HttpSession session = request.getSession(true);
			session.setAttribute("email", email);
			
		}
		
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			String command = request.getParameter("command");
			
			if(command == null) {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("success", false);
				PrintWriter out = response.getWriter();
				out.println(jsonObject);
				return;
			}
			
			switch(command) {
				case "register":
					registerUser(request,response);
					break;
				case "forgotPassword":
					recoverUser(request,response);
					break;
				case "logout":
					logoutUser(request,response);
					break;
				case "setNewPassword":
					setNewPassword(request, response);
					break;
				default:
					loginUser(request,response);
					
			}
			
		} catch(Exception exc) {
			throw new ServletException(exc);
		}
		
	}

	private void setNewPassword(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String password = request.getParameter("password");
		String confirmPassword = request.getParameter("confirmPassword");
		String email = null;
		PrintWriter out = response.getWriter();
		JsonObject jsonObject = new JsonObject();
		
		HttpSession session = request.getSession(false);
		if(session==null) {
			jsonObject.addProperty("success", false);
			out.println(jsonObject);
			out.flush();
			return;
		} else {
			email = (String )session.getAttribute("email");
		}
		
		
		if(password==null || confirmPassword==null || email==null) {
			jsonObject.addProperty("success", false);
			jsonObject.addProperty("error", "Invalid Input");
		} else {
			password=password.trim();
			confirmPassword=confirmPassword.trim();
			boolean error=(validatePasswords(password,confirmPassword)!=null);
			error=error|(validateEmail(email)!=null);
			if(!error) {
				if(!userDBUtil.doesEmailExist(email)) {
				} else if(!userDBUtil.isValidRecovery(email)) {
					error = true;
				}
			}
			if(error) {
				jsonObject.addProperty("success", false);
				jsonObject.addProperty("error", "Invalid Input/Session");
			} else {
				userDBUtil.updatePassword(email,password);
				jsonObject.addProperty("success", true);
				session.invalidate();
			}
		}
		
		out.println(jsonObject);
		out.flush();
		
	}

	private void logoutUser(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		User user = (User) request.getSession().getAttribute("user");
		
		JsonObject jsonObject = new JsonObject();
		
		PrintWriter out = response.getWriter();
		
		response.setCharacterEncoding("UTF-8");
		
		HttpSession session=request.getSession(false);
		if(session!=null) {
//			System.out.println("Invalidating");
			session.invalidate(); 
		}
		
		jsonObject.addProperty("success", true);
		out.println(jsonObject);
		
		out.flush();
		
		
	}

	private void loginUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String email=request.getParameter("email");
		String password=request.getParameter("password");
		
		ArrayList<String> validationErrors = new ArrayList<>();
		JsonObject jsonObject = new JsonObject();
		
		PrintWriter out = response.getWriter();
		
		response.setCharacterEncoding("UTF-8");
 		
		if(email==null || password==null) {
			
			validationErrors.add("Invalid Input");
			
			jsonObject.addProperty("success", false);
			JsonArray errors=new JsonArray();
			for(String error:validationErrors) {
				errors.add(new JsonPrimitive(error));
			}
			
			jsonObject.add("errors", errors);
			
			out.print(jsonObject);
			
			return;
			
			
			
		}
		
		email=email.trim();
		password=password.trim();
		
		String emailError=validateEmail(email);
//		System.out.println(emailError);
		
		if(emailError==null) {
			
			if(!userDBUtil.doesEmailExist(email)) {
				
				emailError="Email does not exist";
				
			}
			
		}
		
		if(emailError!=null) {
			
			validationErrors.add(emailError);
			
		}
		if(password.length()==0) {
			
			validationErrors.add("Invalid Password");
			
		}
		
		if(validationErrors.isEmpty()) {
				
			User user = userDBUtil.performLogin(email,password);
			
			if(user!=null) {
				
				//Start the session
				jsonObject.addProperty("success", true);
				HttpSession session = request.getSession(true);
				session.setAttribute("user", user);
				
				
				
			} else{
				
				validationErrors.add("Incorrect Email/Password Combination");
				
				jsonObject.addProperty("success", false);
				JsonArray errors=new JsonArray();
				for(String error:validationErrors) {
					errors.add(new JsonPrimitive(error));
				}
				
				jsonObject.add("errors", errors);
				
				
				
			}
			
		} else {
			
			jsonObject.addProperty("success", false);
			JsonArray errors=new JsonArray();
			for(String error:validationErrors) {
				errors.add(new JsonPrimitive(error));
			}
			
			jsonObject.add("errors", errors);
			
			
		}
		
		out.print(jsonObject);
		
		out.flush();
		
		
	}

	private void recoverUser(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String email = request.getParameter("email");
		
		ArrayList<String> validationErrors = new ArrayList<>();
		
		JsonObject jsonObject = new JsonObject();
		
		PrintWriter out = response.getWriter();
		
		response.setCharacterEncoding("UTF-8");
		
		if(email == null) {
			validationErrors.add("Invalid Input");
			
			jsonObject.addProperty("success", false);
			JsonArray errors=new JsonArray();
			for(String error:validationErrors) {
				errors.add(new JsonPrimitive(error));
			}
			
			jsonObject.add("errors", errors);
		
		} else {
			
			email = email.trim();
			
			String emailError = validateEmail(email);
			
			if(emailError == null) {
				if(!userDBUtil.doesEmailExist(email)) {
					validationErrors.add("Email does not exist");
					emailError = "Email does not exist";
				} 
			}
			
			if(emailError!=null) {
				
				jsonObject.addProperty("success", false);
				JsonArray errors=new JsonArray();
				for(String error:validationErrors) {
					errors.add(new JsonPrimitive(error));
				}
				
				jsonObject.add("errors", errors);
				
			} else {
				long timeLimit = new Date().getTime()+15*60*1000;
				userDBUtil.setPasswordRecoveryOn(email,timeLimit);
				
				String link=getBaseUrl(request)+"/accounts?command=resetPassword&email="+email;
				String message = "Paste following link in your browser: "+link;
				
				MailUtils.send(email, "Cleeda Password Recovery", message);
				
				jsonObject.addProperty("success", true);
				
			}
			
		}
	
		out.println(jsonObject);
		System.out.println(jsonObject);
		out.flush();
		
	}

	public static String getBaseUrl(HttpServletRequest request) {
	    String scheme = request.getScheme() + "://";
	    String serverName = request.getServerName();
	    String serverPort = (request.getServerPort() == 80) ? "" : ":" + request.getServerPort();
	    String contextPath = request.getContextPath();
	    return scheme + serverName + serverPort + contextPath;
	  }
	
	private void registerUser(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		String email = request.getParameter("email");
		String firstName = request.getParameter("firstName");
		String lastName = request.getParameter("lastName");
		String password=request.getParameter("password");
		String confirmPassword = request.getParameter("confirmPassword");
		
		ArrayList<String> validationErrors=new ArrayList<>();
		
		if(email==null || firstName==null ||lastName==null || password==null || confirmPassword==null) {
			validationErrors.add("Invalid Input");
			return;
		}
		
		email=email.trim();
		firstName=firstName.trim();
		lastName=lastName.trim();
		password=password.trim();
		confirmPassword=confirmPassword.trim();
		
		String emailError = validateEmail(email);
		String passwordError = validatePasswords(password,confirmPassword);
		
		
		
		if(emailError==null) {
			if(userDBUtil.doesEmailExist(email)) {
				emailError = "Given Email already exists";
 			}
		}
		
		if(emailError!=null) {
			validationErrors.add(emailError);
		}
		if(passwordError!=null) {
			validationErrors.add(passwordError);
		}
		
		PrintWriter out = response.getWriter();
		
		response.setCharacterEncoding("UTF-8");
		out.println("{");
		
		
		
		if(validationErrors.isEmpty()) {
			
			boolean success=userDBUtil.createUser(new User(
						firstName,
						lastName,
						email,
						password
					));
			if(!success) {
				out.println("\"success\":false,");
				validationErrors.add("Internal Server Error");
				out.println("\"errors\":[");
				for(int i=0;i<validationErrors.size();++i) {
					out.print("\""+validationErrors.get(i)+"\"");
					if(i!=validationErrors.size()-1) {
						out.println(",");
					}
				}
				out.print("]");
			} else {
				out.println(" \"success\":true");
				String message;
				message="Thank You for registering!";
				MailUtils.send(email, "Welcome to Cleeda", message);
			}
			
		} else {
//			System.out.println("errors are there");
			out.println("\"success\":false,");
			out.println("\"errors\":[");
			for(int i=0;i<validationErrors.size();++i) {
				out.print("\""+validationErrors.get(i)+"\"");
				if(i!=validationErrors.size()-1) {
					out.println(",");
				}
			}
			out.print("]");
			
		}
		
		out.println("}");
		out.flush();
		
	}

	private String validatePasswords(String password, String confirmPassword) {
		
		String message;
		
		if(password.length()<8) {
			message="Password must be atleast 8 characters long";
		} else if(!password.equals(confirmPassword)) {
			message="Both Passwords must match";
		} else {
			message=null;
		}
		
		return message;
		
	}

	private String validateEmail(String email) {
		
		String message = null;
		if(!email.contains(".") || !email.contains(".")) {
			message = "Incorrect Email";
		}
		
		return message;
		
	}

}
