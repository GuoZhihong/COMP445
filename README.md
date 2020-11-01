# Assignment 1:

Notice : for post with inline data example:
                                           
                                           
    I used CURL is httpc post -h Content-Type:application/json --d '{"Assignment": 1}' 'http://httpbin.org/post' instead of httpc post -h Content-Type:application/json --d '{"Assignment": 1}' http://httpbin.org/post to select right URL,so without '' in URL ,all of command line will not work properly.



# Assignment 2:
Server Side:        

                    httpfs -d D:/onedrive/COMP/COMP445/lab/A1   
                    httpfs -v -d D:/onedrive/COMP/COMP445/lab/A1    
                    httpfs -v -p 8080 -d D:/onedrive/COMP/COMP445/lab/A1    


Client side :          
                                          
              A1:httpc get -v 'http://localhost/get?course=networking&assignment=1'
                 httpc post -h Content-Type:application/json --d '{"Assignment": 1}' 'http://localhost/post'



             A2:httpc post -v -h Content-Type:application/json --d '{"Assignment": 210}' 'http://localhost/inputBody.txt'
                httpc post -v -h Content-Type:application/json --d '{"Assignment": 210}' 'http://localhost/xx.txt'
                httpc get -v 'http://localhost'
                httpc get -v 'http://localhost/inputBody.txt'
                httpc get -v 'http://localhost/input.txt'
               
# UDP part refers to https://github.com/GuoZhihong/UDP-Server-Client-Application   
