package com.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.entities.User;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;
import com.helpers.FirebaseHelper;
import com.helpers.RegistrationHelper;
import com.repositories.UserRepository;

@Controller
public class RegistrationController {

	@Autowired
	private UserRepository userDao;
	
	//User reg
	@RequestMapping("/registerUser")
	public void RegisterUser(HttpEntity<String> httpEntity, HttpServletRequest request, HttpServletResponse response, @RequestParam("email") String userEmail) {
		User x = new User();
		x.setEmail(userEmail);
		
		if(userDao.findByEmail(userEmail) == null) {
			userDao.save(x);
		}
	}
	
	//Get users from Firebase
	@RequestMapping("/firebaseUpdate")
	public void FirebaseUpdate(HttpEntity<String> httpEntity, HttpServletRequest request, HttpServletResponse response){
		try {
			ListUsersPage page = FirebaseAuth.getInstance().listUsersAsync(null).get();
			for (ExportedUserRecord user : page.iterateAll()) {
				System.out.println(user.getEmail());
				if(userDao.findByEmail(user.getEmail()) == null) {
					User newUser = new User();
					newUser.setEmail(user.getEmail());
					userDao.save(newUser);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
