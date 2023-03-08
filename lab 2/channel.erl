-module(channel).
-export([start/1,stop/1,handle/2]).

-record(channelstate, {
    users,
    name,
    active
}).

initial_state(Name) ->
    #channelstate{
        users = [],
        name = Name,
        active = false
    }.


start(Name) ->
    genserver:start(list_to_atom(Name), initial_state(Name), fun handle/2).


stop(Name) ->
    genserver:request(list_to_atom(Name), {stop, Name}).


send_message(St, _, _, []) ->
    {reply, ok, St};

%skickar rekursivt till users i xs 
%(listan med resten av användare)
send_message(St, Nick, Msg, [User | Xs]) ->
    User ! {request, self(), make_ref(), {message_receive, St#channelstate.name, Nick, Msg}},
    send_message(St, Nick, Msg, Xs).

%vill ha en message_send, ej skicka till sig själv, 
%tar bort sig själv, listan används i send_message
handle(St, {message_send, Pid, Nick, Msg}) ->

    OtherUsers = lists:delete(Pid, St#channelstate.users),

    send_message(St, Nick, Msg, OtherUsers);

% Skapar användare och skickar request till 
%servern om användaren inte är i users, append
%active sätts till true
handle(St, {join, Pid}) -> 
    case lists:member(Pid, St#channelstate.users) of
        false ->
            NewUsers = [Pid | St#channelstate.users],
            {reply, ok, St#channelstate{users = NewUsers, active = true}};
        true ->
            {reply, {error, user_already_joined, "User is already in channel"}, St}
    end;
% tar bort användaren ur listan av users, sätter till ny lista
handle(St, {leave, Pid}) ->
    NewUsers = lists:delete(Pid, St#channelstate.users),
    {reply, ok, St#channelstate{users = NewUsers}};

%boolean om true eller false, 
%för att kunna i clienten kolla om channel är aktiv
handle(St, {is_active}) ->
    case St#channelstate.active of
        true  -> {reply, active, St};
        false -> {reply, inactive, St}
    end;
%för alla användare, skicka en request med user remove channel
%tar också bort channeln från alla connected users och sen stop
handle(St, {stop, Name}) ->
    lists:foreach(fun(User) ->
        genserver:request(User, {remove_channel, St#channelstate.name})
    end, St#channelstate.users),
    genserver:stop(list_to_atom(Name)),
    {reply, ok, St}.