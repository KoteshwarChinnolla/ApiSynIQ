package main

import (
	"ApiGateway/pkg/textmanipulation"
	"encoding/json"
	"net/http"
	"regexp"
	"strconv"
)

// ----------------------------------
// Main
// ----------------------------------
func main() {
	repo := textmanipulation.NewPageRepo()
	controller := NewController(repo)
	http.Handle("/", controller)
	http.Handle("/allMessages", controller)
	http.Handle("/message/", controller)
	http.Handle("/message", controller)
	http.ListenAndServe(":8080", nil)
}

type Controller struct {
	repo textmanipulation.Repository // Dependency Injection
}

// Constructor Injection
func NewController(service textmanipulation.Repository) *Controller {
	return &Controller{repo: service}
}

var (
	allMessageRe    = regexp.MustCompile(`^/allMessages/*$`)
	messageRe       = regexp.MustCompile(`^/message/*$`)
	messageReWithID = regexp.MustCompile(`^/message/([0-9]+)$`)
)

// Request Routing
func (c *Controller) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	switch {
	case allMessageRe.MatchString(r.URL.Path) && r.Method == "GET":
		c.getAllMessages(w, r)
	case messageReWithID.MatchString(r.URL.Path) && r.Method == "DELETE":
		c.deleteMessage(w, r)
	case messageReWithID.MatchString(r.URL.Path) && r.Method == "PUT":
		c.updateMessage(w, r)
	case messageRe.MatchString(r.URL.Path) && r.Method == "POST":
		c.addMessage(w, r)
	case messageReWithID.MatchString(r.URL.Path) && r.Method == "GET":
		c.getMessage(w, r)
	case r.URL.Path == "/":
		w.Write([]byte("Hello World"))
	default:
		http.Error(w, "Not Found", http.StatusNotFound)
	}
}

// Controller Implementation
func (c *Controller) getAllMessages(w http.ResponseWriter, r *http.Request) {
	messages := c.repo.GetAll()
	if messages == nil {
		w.WriteHeader(http.StatusNoContent)
		return
	}
	json, err := json.Marshal(messages)
	if err != nil {
		w.WriteHeader(http.StatusNotAcceptable)
		w.Write([]byte(err.Error()))
		return
	}
	w.Write([]byte(json))
}

func (c *Controller) getMessage(w http.ResponseWriter, r *http.Request) {
	matches := messageReWithID.FindStringSubmatch(r.URL.Path)
	if matches == nil {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	idStr := matches[1]
	id, err := strconv.ParseInt(idStr, 10, 64)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(err.Error()))
		return
	}
	messages := c.repo.GetById(int(id))
	json, err := json.Marshal(messages)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(err.Error()))
		return
	}
	w.Write([]byte(json))
}

func (c *Controller) addMessage(w http.ResponseWriter, r *http.Request) {
	var message textmanipulation.SimpleMessage
	err := json.NewDecoder(r.Body).Decode(&message)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(err.Error()))
		return
	}
	messages := c.repo.AddMessage(message)
	json, err := json.Marshal(messages)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(err.Error()))
		return
	}
	w.Write([]byte(json))
}

func (c *Controller) deleteMessage(w http.ResponseWriter, r *http.Request) {
	matches := messageReWithID.FindStringSubmatch(r.URL.Path)
	if matches == nil {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	idStr := matches[1]
	id, err := strconv.ParseInt(idStr, 10, 64)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(err.Error()))
		return
	}
	messages := c.repo.DeleteMessage(int(id))
	json, err := json.Marshal(messages)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(err.Error()))
		return
	}
	w.Write([]byte(json))
}

func (c *Controller) updateMessage(w http.ResponseWriter, r *http.Request) {
	var message textmanipulation.SimpleMessage
	err := json.NewDecoder(r.Body).Decode(&message)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(err.Error()))
		return
	}
	matches := messageReWithID.FindStringSubmatch(r.URL.Path)
	if matches == nil {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	idStr := matches[1]
	id, err := strconv.ParseInt(idStr, 10, 64)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(err.Error()))
		return
	}
	messages := c.repo.UpdateMessage(int(id), message)
	json, err := json.Marshal(messages)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(err.Error()))
		return
	}
	w.Write([]byte(json))
}
