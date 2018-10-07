Notice : for post with inline data example:
I used CURL is httpc post -h Content-Type:application/json --d '{"Assignment": 1}' 'http://httpbin.org/post'
instead of httpc post -h Content-Type:application/json --d '{"Assignment": 1}' http://httpbin.org/post
to select right URL,
so without '' in URL ,all of command line will not work properly.
