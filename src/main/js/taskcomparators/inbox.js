function compare(s1,s2) {
	log("s1: "+s1);
	log("s2: "+s2);
	var res = compareObjects(JSON.parse(s1),JSON.parse(s2));
	log("final res: "+res);
	return res;
}
