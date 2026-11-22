public class Example {
    public static void main(String[] args)
    {
        /*
        If you aren't familiar, special characters like '@' are delimiters in URIs, so in cases
        like below where we're using '@' not as a delimiter, but as part of a string representing
        an email address, we have to use '%40'.

        For the time being, assume URIs will follow the implied paths in the SOW.

        As of now, you need to manually create these strings. This is likely not a service
        we have time to provide for you guys, but there are (from what I briefly researched)
        a few libraries that let you build URIs dynamically.
         */
        String GET_requestURI = "http://localhost:8080/users?email=phos.p%40mail";
        
        String POST_requestURI = "http://localhost:8080/users";
        String POST_body = "{\"first_name\":\"luigi\",\"last_name\":\"mario\",\"email\":\"luigi.m@mail\",\"hashed_password\":\"123\",\"role\":\"student\"}";

        String PUT_requestURI = "http://localhost:8080/users?email=phos.p%40mail";
        String PUT_body = "{\"first_name\":\"joe\",\"last_name\":\"shmoe\"}";
       
        HttpWrapper httpW;

        httpW = new HttpWrapper(GET_requestURI, null, HttpWrapper.RequestType.GET);
        System.out.println(httpW.go());

        httpW = new HttpWrapper(POST_requestURI, POST_body, HttpWrapper.RequestType.POST);
        System.out.println(httpW.go());

        httpW = new HttpWrapper(PUT_requestURI, PUT_body, HttpWrapper.RequestType.PUT);
        System.out.println(httpW.go());
    }
}