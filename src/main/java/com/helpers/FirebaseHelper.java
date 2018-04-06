package com.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import com.entities.User;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;
import com.repositories.UserRepository;

@Controller
public class FirebaseHelper {

	@Autowired private UserRepository userDao;

	public void InitializeUserTable () {
		try {
			ListUsersPage page = FirebaseAuth.getInstance().listUsersAsync(null).get();
			for (ExportedUserRecord user : page.iterateAll()) {
				System.out.println(user.getEmail());
				try {
					User x = userDao.findByEmail(user.getEmail());
				} catch (Exception e) {
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
