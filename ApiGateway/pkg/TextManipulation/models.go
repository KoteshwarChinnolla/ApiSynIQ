package textmanipulation

type ListSimpleMessage struct {
	Messages []SimpleMessage
}

type SimpleMessage struct {
	Id   int
	Text string
}

func NewSimpleMessage(id int, text string) SimpleMessage {
	return SimpleMessage{Id: id, Text: text}
}

func NewListSimpleMessage(messages []SimpleMessage) ListSimpleMessage {
	return ListSimpleMessage{Messages: messages}
}

func (l *ListSimpleMessage) addMessage(message SimpleMessage) []SimpleMessage {
	l.Messages = append(l.Messages, message)
	return l.Messages
}
