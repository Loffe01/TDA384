-module(channel).
-export([start/1,stop/1,handle/2]).

-record(channelstate, {
    users,
    name,
    active

}).

initialize_channel(Name)->
    #channelstate{
    users = [],
    name= Name,
    active=false
    }.

start(Name) ->
    genserver:start(list_to_atom(Name), initialize_channel(Name), fun handle/2).


stop(Name) ->
    genserver:request(list_to_atom(Name), {stop, Name}).


%recurisvley sends messages to users in list
send_message(St, Nick, Msg, [User | Xs]) ->
    User ! {request, self(), make_ref(), {message_receive, St#channelstate.name, Nick, Msg}},
    send_message(St, Nick, Msg, Xs).

handle(St, {message_send, Pid, Nick, Msg}) ->

    OtherUsers= lists:delete(Pid, St#channelstate.users),

    send_message(St, Nick, Msg, OtherUsers);


handle(St, {join, Pid}) ->
   case lists:member(Pid, St#channelstate.users) of 
        false ->
            NewUsers = [Pid | St#channelstate.users],
            {reply, ok, St#channelstate {users= NewUsers, active = true}};
        true ->
            {reply,{error,user_already_joined, "User is already in channel"}, St}
        
    end;


handle(St, {leave, Pid}) ->
    NewUsers = lists:delete(Pid, St#channelstate.users),
    {reply, ok, St#channelstate {users= NewUsers}};

handle(St, {is_active}) ->
    case St#channelstate.active of 
        true -> {reply, active, St};
        false -> {reply, inactive, St}
end;

handle (St, {stop, Name}) ->
    lists:foreach(fun(users) ->
        genserver:request(user, {remove_channel, St#channelstate.name})
    end, St#channelstate.users),
    genserver:stop(list_to_atom(Name)),
    {reply, ok,St}.