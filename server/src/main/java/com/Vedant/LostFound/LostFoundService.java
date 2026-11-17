package com.Vedant.LostFound;

import org.springframework.stereotype.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.*;
import org.springframework.mail.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.internet.*;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

@Service
class LostFoundService{
	
	private Map<String, Long> sent = new ConcurrentHashMap<>();
	private static final long WINDOW = 24L * 60 * 60 * 1000;

	private JavaMailSender jms;
	private TemplateEngine te;
	
	@Value("${lostfound.admin.email}")
	private String adminEmail;
	
	public LostFoundService(JavaMailSender jms, TemplateEngine te){
		this.jms=jms;
		this.te=te;
	}
	
	public String get(Map<String,String> data, String key, int max){
		String v = data.get(key);
		if(v==null) return "";
		v = v.trim();
		return v.length() > max ? v.substring(0, max) : v;
	}

	public Map<String,String> report(Map<String,String> data){
		Map<String, String> result = new HashMap<>();
		String key = null;
		try{
			String type = get(data,"type",10).toUpperCase();
			String name = get(data,"name",60).replaceAll("[\\r\\n]+", " ");
			String uemail = get(data,"email",100);
			String item = get(data,"itemName",80).replaceAll("[\\r\\n]+", " ");
			String category = get(data,"category",30);
			String place = get(data,"place",40);
			String date = get(data, "date", 12);
			String keptAt = get(data, "keptAt", 80);
			String description = get(data, "description", 500);
		
			if (!type.equals("LOST") && !type.equals("FOUND")) {
				result.put("status", "error");
				result.put("errmsg", "Type must be Lost or Found.");
				return result;
			}
		
			if(name.isEmpty() || item.isEmpty() || place.isEmpty() || date.isEmpty() || description.isEmpty()){
				result.put("status","error");
				result.put("errmsg","Please fill all required Fields");
				return result;
			}
			
			if(!uemail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")){
				result.put("status","error");
				result.put("errmsg","Enter a Valid Email Address");
				return result;
			}

			long now = System.currentTimeMillis();
			sent.values().removeIf(t -> now - t > WINDOW);
			key = (uemail + "|" + type + "|" + item + "|" + place + "|" + date).toLowerCase();
			Long prev = sent.putIfAbsent(key, now);

			if(prev != null){
				key = null;				result.put("status","error");
				result.put("errmsg","You have already sent this report. Please check your email for the confirmation.");
				return result;
			}
			String refId = "LF-" + (1000 + new SecureRandom().nextInt(9000));
			
			Context cadmin = new Context();
			cadmin.setVariable("refId", refId);
			cadmin.setVariable("type", type);
			cadmin.setVariable("name", name);
			cadmin.setVariable("email", uemail);
			cadmin.setVariable("item", item);
			cadmin.setVariable("category", category);
			cadmin.setVariable("place", place);
			cadmin.setVariable("date", date);
			cadmin.setVariable("keptAt", keptAt);
			cadmin.setVariable("description", description);

			String sadmin = te.process("admin.html",cadmin);
			MimeMessage m1 = jms.createMimeMessage();
			MimeMessageHelper mh1 = new MimeMessageHelper(m1,true);
			mh1.setTo(adminEmail);
			mh1.setReplyTo(uemail);
			mh1.setSubject("[" + type + "] " + item + " (" + refId + ")");
			mh1.setText(sadmin, true);
			jms.send(m1);

			Context cuser = new Context();
			cuser.setVariable("refId",refId);
			cuser.setVariable("type",type);
			cuser.setVariable("name",name);
			cuser.setVariable("item",item);

			String suser = te.process("user.html",cuser);
			MimeMessage m2 = jms.createMimeMessage();
			MimeMessageHelper mh2 = new MimeMessageHelper(m2,true);
			mh2.setTo(uemail);
			mh2.setSubject("We Received your report (" + refId + ")");
			mh2.setText(suser,true);
			jms.send(m2);

			result.put("status","success");
			result.put("reportId", refId);
			result.put("msg", "Report sent. Check your email for confirmation.");
		}
		catch(Exception e){
			if(key != null) sent.remove(key);
			System.out.println("Mail failed: " + e.getMessage());
			result.put("status", "error");
			result.put("errmsg", "Could not send the email right now. Please try again later.");
		}	
		return result;
	
	}

}
