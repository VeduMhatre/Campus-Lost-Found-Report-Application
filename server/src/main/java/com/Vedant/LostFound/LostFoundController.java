package com.Vedant.LostFound;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@CrossOrigin(origins="*")

public class LostFoundController{

	private LostFoundService service;
	public LostFoundController(LostFoundService service){
		this.service=service;
	}
	
	@PostMapping("/report")
	public Map<String,String> report(@RequestBody Map<String,String> data){
		return service.report(data);
	}
	

}
