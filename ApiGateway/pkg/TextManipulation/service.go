package textmanipulation

type Repository interface {
	GetAll() []SimpleMessage
	GetById(id int) SimpleMessage
	AddMessage(messages SimpleMessage) []SimpleMessage
	DeleteMessage(id int) []SimpleMessage
	UpdateMessage(id int, message SimpleMessage) []SimpleMessage
}

type PageRepo struct{}

func NewPageRepo() Repository {
	return &PageRepo{}
}

func (p *PageRepo) GetAll() []SimpleMessage {
	return []SimpleMessage{
		{Id: 1, Text: "Hello Alice"},
		{Id: 2, Text: "Hello Bob"},
		{Id: 3, Text: "Hello Charlie"},
	}
}

func (p *PageRepo) GetById(id int) SimpleMessage {
	messages := []SimpleMessage{}
	for _, message := range p.GetAll() {
		if message.Id == id {
			messages = append(messages, message)
		}
	}
	return messages[0]
}

func (p *PageRepo) AddMessage(message SimpleMessage) []SimpleMessage {
	messages := p.GetAll()
	messages = append(messages, message)
	return messages
}

func (p *PageRepo) DeleteMessage(id int) []SimpleMessage {
	messages := []SimpleMessage{}
	for _, message := range p.GetAll() {
		if message.Id != id {
			messages = append(messages, message)
		}
	}
	return messages
}

func (p *PageRepo) UpdateMessage(id int, message SimpleMessage) []SimpleMessage {
	messages := []SimpleMessage{}
	for _, m := range p.GetAll() {
		if m.Id == id {
			messages = append(messages, message)
		} else {
			messages = append(messages, m)
		}
	}
	return messages
}
