-module(server).
-export([start/1,stop/1]).

% Start a new server process with the given name
% Do not change the signature of this function.
-record(serverlist, {
    channels,
    users
}).

initializingRecord() ->
    #serverlist{
    channels = [],
    users= []
    }.

start(ServerAtom) ->
    genserver:start(ServerAtom, initializingRecord(), fun handle/2).    


stop(ServerAtom) -> 
    genserver:request(ServerAtom, {stop, ServerAtom}).




handle(St, {join, Pid, Nick, Channel}) ->
    
    NewChannels =
        case  lists:member(Channel, St#serverlist.channels) of
            false -> 
                channel:start(Channel),
                [Channel | St#serverlist.channels];

            true -> St#serverlist.channels 
        end,


    NewUsers=
        case (lists:member(Nick, St#serverlist.users)) of 
            false ->
                [Nick | St#serverlist.users];

            true -> St#serverlist.users
        end,


    Reply = genserver:request(list_to_atom(Channel), {join, Pid}),
    {reply, Reply, St#serverlist{channels= NewChannels, users= NewUsers}};
     

handle(St, {stop, ServerAtom}) ->
    lists:foreach(fun(Channel)->
         channel:stop(Channel)
        end, St#serverlist.channels),
        genserver:stop(ServerAtom),
    {reply,ok,St}.


    % TODO Implement function
    % - Spawn a new process which waits for a message, handles it, then loops infinitely
    % - Register this process to ServerAtom
    % - Return the process ID


% Stop the server process registered to the given name,
% together with any other associated processes

